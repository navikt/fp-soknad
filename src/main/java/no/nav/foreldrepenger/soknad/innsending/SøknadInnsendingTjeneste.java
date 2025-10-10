package no.nav.foreldrepenger.soknad.innsending;

import static no.nav.foreldrepenger.soknad.innsending.fordel.BehandleSøknadTask.FORSENDELSE_ID_PROPERTY;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.innsending.fordel.BehandleEttersendelseTask;
import no.nav.foreldrepenger.soknad.innsending.fordel.BehandleSøknadTask;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AdopsjonDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.BarnDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.OmsorgsovertakelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggInnsendingType;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.EttersendelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.YtelseType;
import no.nav.foreldrepenger.soknad.mellomlagring.MellomlagringTjeneste;
import no.nav.foreldrepenger.soknad.mellomlagring.YtelseMellomlagringType;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class SøknadInnsendingTjeneste implements InnsendingTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(SøknadInnsendingTjeneste.class);
    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();
    private static final Environment ENV = Environment.current();

    private MellomlagringTjeneste mellomlagringTjeneste;
    private InnloggetBruker innloggetBruker;
    private DokumentRepository dokumentRepository;
    private ProsessTaskTjeneste prosessTaskTjeneste;

    public SøknadInnsendingTjeneste() {
        //CDI
    }
    @Inject
    public SøknadInnsendingTjeneste(MellomlagringTjeneste mellomlagringTjeneste,
                                    InnloggetBruker innloggetBruker,
                                    DokumentRepository dokumentRepository,
                                    ProsessTaskTjeneste prosessTaskTjeneste) {
        this.mellomlagringTjeneste = mellomlagringTjeneste;
        this.innloggetBruker = innloggetBruker;
        this.dokumentRepository = dokumentRepository;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void lagreSøknadInnsending(SøknadDto søknad) {
        if (erForsendelseAlleredeMottatt(søknad)) {
            LOG.info("Duplikat forsendelse av søknad mottatt for bruker, avbryter lagring og behandling");
            return; // Unngå lagring og behandling av duplikat forsendelse
        }

        var forsendelseId = UUID.randomUUID();
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer(innloggetBruker.brukerFraKontekst())
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(forsendelsesTidspunkt(søknad.mottattdato()))
            .build();
        dokumentRepository.lagre(metadata);

        var søknadDokument = DokumentEntitet.builder()
            .setDokumentInnhold(getInnhold(søknad), ArkivFilType.JSON)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(utledDokumentType(søknad))
            .build();
        dokumentRepository.lagre(søknadDokument);

        var vedleggDokumenter = hentAlleVedlegg(søknad.vedlegg(), finnYtelseType(søknad)).stream()
            .map(v -> lagDokumentFraVedlegg(v.innhold(), forsendelseId, v.skjemanummer(), v.begrunnelse()))
            .toList();
        vedleggDokumenter.forEach(dokumentRepository::lagre);

        var task = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        prosessTaskTjeneste.lagre(task);
    }

    @Override
    public void lagreEttersendelseInnsending(EttersendelseDto ettersendelse) {
        var vedleggMedInnhold = hentAlleVedlegg(ettersendelse.vedlegg(), tilYtelseTypeMellomlagring(ettersendelse.type()));
        if (erEttersendelseAlleredeMottatt(vedleggMedInnhold)) {
            LOG.info("Duplikat forsendelse av ettersendelse mottatt for bruker, avbryter lagring og behandling");
            return; // Unngå lagring og behandling av duplikat forsendelse
        }

        var forsendelseId = UUID.randomUUID();
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer(innloggetBruker.brukerFraKontekst())
            .setSaksnummer(ettersendelse.saksnummer().value())
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(forsendelsesTidspunkt(ettersendelse.mottattdato()))
            .build();
        dokumentRepository.lagre(metadata);

        var vedleggDokumenter = vedleggMedInnhold.stream()
            .map(v -> lagDokumentFraVedlegg(v.innhold(), forsendelseId, v.skjemanummer(), v.begrunnelse()))
            .toList();
        vedleggDokumenter.forEach(dokumentRepository::lagre);

        var task = ProsessTaskData.forProsessTask(BehandleEttersendelseTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        prosessTaskTjeneste.lagre(task);
    }

    @Override
    public void lagreUttalelseOmTilbakekreving(EttersendelseDto ettersendelse) {
        var uttalelseOmTilbakebetaling = new UtalelseOmTilbakebetaling(ettersendelse.type(), ettersendelse.brukerTekst());
        if (erForsendelseAlleredeMottatt(uttalelseOmTilbakebetaling)) {
            LOG.warn("Duplikat forsendelse av svar på uttalelse om tilbakebetaling oppdaget for bruker, avbryter lagring og behandling");
            return; // Unngå lagring og behandling av duplikat forsendelse
        }

        var forsendelseId = UUID.randomUUID();
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer(innloggetBruker.brukerFraKontekst())
            .setSaksnummer(ettersendelse.saksnummer().value())
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(forsendelsesTidspunkt(ettersendelse.mottattdato()))
            .build();
        dokumentRepository.lagre(metadata);

        var uttalelseDokument = DokumentEntitet.builder()
            .setDokumentInnhold(getInnhold(uttalelseOmTilbakebetaling), ArkivFilType.JSON)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000119)
            .build();
        dokumentRepository.lagre(uttalelseDokument);

        var task = ProsessTaskData.forProsessTask(BehandleEttersendelseTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        prosessTaskTjeneste.lagre(task);
    }

    private boolean erForsendelseAlleredeMottatt(UtalelseOmTilbakebetaling utalelseOmTilbakebetaling) {
        var eksisterendeForsendelse = dokumentRepository.hentForsendelse(innloggetBruker.brukerFraKontekst()).stream()
            .max(Comparator.comparing(ForsendelseEntitet::getForsendelseMottatt));
        if (eksisterendeForsendelse.isEmpty()) {
            return false;
        }

        var eksisterendeUttalelseOmTilbakebetaling = dokumentRepository.hentDokumenter(eksisterendeForsendelse.get().getForsendelseId()).stream()
            .filter(DokumentEntitet::erUttalelseOmTilbakebetaling)
            .findFirst();

        if (eksisterendeUttalelseOmTilbakebetaling.isEmpty()) {
            return false;
        }

        var eksisterendeUttalelsePåTilbakebetaling = SøknadJsonMapper.deseraliserUttalelsePåTilbakebetaling(eksisterendeUttalelseOmTilbakebetaling.get());
        return utalelseOmTilbakebetaling.equals(eksisterendeUttalelsePåTilbakebetaling);
    }

    private boolean erEttersendelseAlleredeMottatt(List<VedleggSkjemanummerWrapper> ettersendteVedlegg) {
        var eksisterendeForsendelse = dokumentRepository.hentForsendelse(innloggetBruker.brukerFraKontekst()).stream()
            .max(Comparator.comparing(ForsendelseEntitet::getForsendelseMottatt));
        if (eksisterendeForsendelse.isEmpty()) {
            return false;
        }

        var eksisterendeEttersendelser = dokumentRepository.hentDokumenter(eksisterendeForsendelse.get().getForsendelseId()).stream()
            .filter(dokumentEntitet -> !dokumentEntitet.erSøknad())
            .toList();

        if (eksisterendeEttersendelser.isEmpty() || eksisterendeEttersendelser.size() != ettersendteVedlegg.size()) {
            return false;
        }

        return ettersendteVedlegg.stream().allMatch(vedlegg ->
            eksisterendeEttersendelser.stream().anyMatch(eksistrende ->
                    eksistrende.getDokumentTypeId().equals(vedlegg.skjemanummer()) && Arrays.equals(eksistrende.getByteArrayDokument(), vedlegg.innhold())
                )
        );
    }

    private boolean erForsendelseAlleredeMottatt(SøknadDto søknad) {
        var eksisterendeForsendelse = dokumentRepository.hentForsendelse(innloggetBruker.brukerFraKontekst()).stream()
            .max(Comparator.comparing(ForsendelseEntitet::getForsendelseMottatt));
        if (eksisterendeForsendelse.isEmpty()) {
            return false;
        }

        var eksisterendeSøknad = dokumentRepository.hentDokumenter(eksisterendeForsendelse.get().getForsendelseId(), ArkivFilType.JSON).stream()
            .filter(DokumentEntitet::erSøknad)
            .findFirst();
        return eksisterendeSøknad.filter(dokumentEntitet -> Arrays.equals(dokumentEntitet.getByteArrayDokument(), getInnhold(søknad))).isPresent();
    }

    private static LocalDateTime forsendelsesTidspunkt(LocalDateTime forsendelsesTidspunkt) {
        if (ENV.isProd() || forsendelsesTidspunkt == null) {
            return LocalDateTime.now();
        }
        return forsendelsesTidspunkt; // Brukes av autotest for å spesifisere mottatttidspunkt annet enn dagens dato
    }

    private static DokumentEntitet lagDokumentFraVedlegg(byte[] v, UUID forsendelseId, DokumentTypeId skjemanummer, Optional<String> begrunnelse) {
        return DokumentEntitet.builder()
            .setDokumentInnhold(v, ArkivFilType.PDFA)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(skjemanummer)
            .setBeskrivelse(begrunnelse.orElse(null))
            .build();
    }

    private static YtelseMellomlagringType tilYtelseTypeMellomlagring(YtelseType type) {
        return switch (type) {
            case ENGANGSSTØNAD -> YtelseMellomlagringType.ENGANGSSTONAD;
            case FORELDREPENGER -> YtelseMellomlagringType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseMellomlagringType.SVANGERSKAPSPENGER;
        };
    }

    private static DokumentTypeId utledDokumentType(SøknadDto dto) {
        return switch (dto) {
            case ForeldrepengesøknadDto fp-> erAdopsjonEllerOmsorgsovertakelse(fp.barn()) ? DokumentTypeId.I000002 : DokumentTypeId.I000005;
            case EndringssøknadForeldrepengerDto ignored -> DokumentTypeId.I000050;
            case EngangsstønadDto es ->  erAdopsjonEllerOmsorgsovertakelse(es.barn()) ? DokumentTypeId.I000004 : DokumentTypeId.I000003;
            case SvangerskapspengesøknadDto ignored ->  DokumentTypeId.I000001;
        };
    }

    private static boolean erAdopsjonEllerOmsorgsovertakelse(BarnDto barnDto) {
        return barnDto instanceof AdopsjonDto || barnDto instanceof OmsorgsovertakelseDto;
    }

    private static byte[] getInnhold(Object object) {
        try {
            return MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // TODO: Exceaption handling
        }
    }

    private static YtelseMellomlagringType finnYtelseType(SøknadDto dto) {
        return switch (dto) {
            case ForeldrepengesøknadDto ignored -> YtelseMellomlagringType.FORELDREPENGER;
            case EndringssøknadForeldrepengerDto ignored -> YtelseMellomlagringType.FORELDREPENGER;
            case EngangsstønadDto ignored ->  YtelseMellomlagringType.ENGANGSSTONAD;
            case SvangerskapspengesøknadDto ignored ->  YtelseMellomlagringType.SVANGERSKAPSPENGER;
        };
    }

    private List<VedleggSkjemanummerWrapper> hentAlleVedlegg(List<VedleggDto> vedleggene, YtelseMellomlagringType ytelseMellomlagringType) {
        return vedleggene.stream()
            .filter(v -> VedleggInnsendingType.LASTET_OPP.equals(v.innsendingsType()))
            .map(v -> getVedleggSkjemanummerWrapper(ytelseMellomlagringType, v))
            .toList();
    }

    private VedleggSkjemanummerWrapper getVedleggSkjemanummerWrapper(YtelseMellomlagringType ytelseType, VedleggDto vedleggDto) {
        var innhold = mellomlagringTjeneste.lesKryptertVedlegg(vedleggDto.uuid().toString(), ytelseType).orElseThrow(() -> new IllegalStateException("Fant ikke mellomlagret vedlegg med uuid " + vedleggDto.uuid()));
        return new VedleggSkjemanummerWrapper(innhold, vedleggDto.skjemanummer(), DokumentTypeId.I000060.equals(vedleggDto.skjemanummer()) ? Optional.of(vedleggDto.beskrivelse()) : Optional.empty());
    }

    private record VedleggSkjemanummerWrapper(byte[] innhold, DokumentTypeId skjemanummer, Optional<String> begrunnelse) {}
}
