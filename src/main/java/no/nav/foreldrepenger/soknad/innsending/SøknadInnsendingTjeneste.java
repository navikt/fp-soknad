package no.nav.foreldrepenger.soknad.innsending;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.BehandleEttersendelseTask;
import no.nav.foreldrepenger.soknad.innsending.fordel.BehandleSøknadTask;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AdopsjonDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.BarnDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.OmsorgsovertakelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggReferanse;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.EttersendelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.YtelseType;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.mellomlagring.MellomlagringTjeneste;
import no.nav.foreldrepenger.soknad.mellomlagring.YtelseMellomlagringType;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;
import no.nav.foreldrepenger.soknad.vedlegg.VedleggTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static no.nav.foreldrepenger.soknad.innsending.fordel.BehandleSøknadTask.FORSENDELSE_ID_PROPERTY;

@ApplicationScoped
public class SøknadInnsendingTjeneste {

    private MellomlagringTjeneste mellomlagringTjeneste;
    private InnloggetBruker innloggetBruker;
    private DokumentRepository dokumentRepository;
    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();
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

    public void lagreEttersendelseInnsending(EttersendelseDto ettersendelse) {
        var forsendelseId = UUID.randomUUID();  // TODO: skal denne komme fra FE?

        var metadata = DokumentMetadata.builder()
            .setBrukerId(innloggetBruker.brukerFraKontekst())
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        dokumentRepository.lagre(metadata);

        var vedleggDokumenter = hentAlleVedlegg(ettersendelse.vedlegg(), tilYtelseTypeMellomlagring(ettersendelse.type())).stream()
            .map(v -> lagDokumentFraVedlegg(v.innhold(), forsendelseId, v.skjemanummer()))
            .toList();
        vedleggDokumenter.forEach(dokumentRepository::lagre);

        var task = ProsessTaskData.forProsessTask(BehandleEttersendelseTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        prosessTaskTjeneste.lagre(task);
    }

    public void lagreSøknadInnsending(SøknadDto søknad) {
        var forsendelseId = UUID.randomUUID();  // TODO: skal denne komme fra FE?

        var metadata = DokumentMetadata.builder()
            .setBrukerId(innloggetBruker.brukerFraKontekst())
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();

        var søknadDokument = Dokument.builder()
            .setDokumentInnhold(getInnhold(søknad), ArkivFilType.JSON)
            .setHovedDokument(true)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(utledDokumentType(søknad))
            .build();

        dokumentRepository.lagre(metadata);
        dokumentRepository.lagre(søknadDokument);

        var vedleggDokumenter = hentAlleVedlegg(søknad.vedlegg(), finnYtelseType(søknad)).stream()
            .map(v -> lagDokumentFraVedlegg(v.innhold(), forsendelseId, v.skjemanummer()))
            .toList();
        vedleggDokumenter.forEach(dokumentRepository::lagre);

        var task = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        prosessTaskTjeneste.lagre(task);
    }

    private static Dokument lagDokumentFraVedlegg(byte[] v, UUID forsendelseId, DokumentTypeId skjemanummer) {
        return Dokument.builder()
            .setDokumentInnhold(v, ArkivFilType.PDFA)
            .setHovedDokument(false)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(skjemanummer)
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
            case EngangsstønadDto es ->  erAdopsjonEllerOmsorgsovertakelse(es.barn()) ? DokumentTypeId.I000004 : DokumentTypeId.I000003;
            case SvangerskapspengesøknadDto ignored ->  DokumentTypeId.I000001;
            case EndringssøknadForeldrepengerDto fpEndring -> erAdopsjonEllerOmsorgsovertakelse(fpEndring.barn()) ? DokumentTypeId.I000002 : DokumentTypeId.I000005;
        };
    }

    private static boolean erAdopsjonEllerOmsorgsovertakelse(BarnDto barnDto) {
        return barnDto instanceof AdopsjonDto || barnDto instanceof OmsorgsovertakelseDto;
    }

    private static byte[] getInnhold(SøknadDto søknad) {
        try {
            return MAPPER.writeValueAsBytes(søknad);
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
        return vedleggene.stream().map(v -> getVedleggSkjemanummerWrapper(ytelseMellomlagringType, v)).toList();
    }

    private VedleggSkjemanummerWrapper getVedleggSkjemanummerWrapper(YtelseMellomlagringType ytelseType, VedleggDto vedleggDto) {
        var referanse = vedleggDto.referanse();
        var innhold = mellomlagringTjeneste.lesKryptertVedlegg(referanse.verdi(), ytelseType).orElseThrow(() -> new IllegalStateException("Fant ikke mellomlagret vedlegg med uuid " + referanse.verdi()));
        return new VedleggSkjemanummerWrapper(referanse, innhold, vedleggDto.skjemanummer());
    }

    private record VedleggSkjemanummerWrapper(VedleggReferanse referanse, byte[] innhold, DokumentTypeId skjemanummer) {}
}
