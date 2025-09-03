package no.nav.foreldrepenger.soknad.innsending.fordel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.Destinasjon;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.DestinasjonsRuter;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.OpprettetJournalpost;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivUtil.mapDokumenttype;
import static no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivUtil.utledHovedDokumentType;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus.FPSAK;

@ApplicationScoped
@ProsessTask(value = "fordeling.behandleDokumentForsendelse", maxFailedRuns = 4, firstDelay = 10, thenDelay = 30)
public class BehandleDokumentforsendelseTask implements ProsessTaskHandler {

    public static final String FORSENDELSE_ID_PROPERTY = "forsendelseId";
    public static final String SAKSNUMMER_PROPERTY = "saksnummer";
    public static final String DOKUMENT_TYPE_ID_PROPERTY = "behandlingTema";
    public static final String BEHANDLING_TEMA_PROPERTY = "dokumentTypeId";

    private static final Logger LOG = LoggerFactory.getLogger(BehandleDokumentforsendelseTask.class);

    private DokumentRepository dokumentRepository;
    private DestinasjonsRuter ruter;
    private FpsakTjeneste fpsakTjeneste;
    private ArkivTjeneste arkivTjeneste;
    private ProsessTaskTjeneste taskTjeneste;

    public BehandleDokumentforsendelseTask() {
        // for CDI
    }

    @Inject
    public BehandleDokumentforsendelseTask(DokumentRepository dokumentRepository, DestinasjonsRuter ruter, FpsakTjeneste fpsakTjeneste,
                                           ArkivTjeneste arkivTjeneste, ProsessTaskTjeneste taskTjeneste) {
        this.dokumentRepository = dokumentRepository;
        this.ruter = ruter;
        this.fpsakTjeneste = fpsakTjeneste;
        this.arkivTjeneste = arkivTjeneste;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(FORSENDELSE_ID_PROPERTY));

        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        var hovedDokument = dokumenter.stream().filter(Dokument::erHovedDokument).findFirst();
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);

        // TODO: Setter mange verdier, og henter informasjon ut fra SØKNAD og setter i wrapper som brukes videre i mye logikk...
        // setFellesWrapperAttributter(w, hovedDokument.orElse(null), metadata);

        var fagsakInfoOpt = fagsakInformasjon(metadata);
        var dokumentTypeId = utledDokumentTypeId(hovedDokument, dokumenter);
        var behandlingTema = utledBehandlingstema(hovedDokument, dokumentTypeId, fagsakInfoOpt);
        var destinasjon = utledDestinasjonForForsendelse(metadata, behandlingTema, fagsakInfoOpt);
        var opprettetJournalpost = opprettJournalpostFerdigstillHvisSaksnummer(forsendelseId, metadata.getBrukerId(), destinasjon);

        // Ikke nødvendig kanskje?
        // if (!journalpost.ferdigstilt()) {
        //     // Det vil komme en Kafka-hendelse om noen sekunder - denne sørger for at vi ikke trigger på den.
        //     dokumentRepository.lagreJournalpostLokal(null, metadata.getForsendelseId().toString(), , "MIDLERTIDIG");
        // }

        dokumentRepository.oppdaterForsendelseMetadata(forsendelseId, opprettetJournalpost.journalpostId(), destinasjon);
        utledNesteSteg(opprettetJournalpost, behandlingTema, dokumentTypeId, forsendelseId, destinasjon);
    }

    private DokumentTypeId utledDokumentTypeId(Optional<Dokument> hovedDokument, List<Dokument> dokumenter) {
        if (hovedDokument.isPresent()) {
            return hovedDokument.get().getDokumentTypeId();
        }

        return utledHovedDokumentType(dokumenter.stream().map(Dokument::getDokumentTypeId).collect(Collectors.toSet()));
    }

    private static BehandlingTema utledBehandlingstema(Optional<Dokument> hovedDokument, DokumentTypeId dokumentTypeId, Optional<FagsakInfomasjonDto> fagsakInfoOpt) {
        if (fagsakInfoOpt.isPresent() && hovedDokument.isEmpty()) { // Ettersendelse?
            return BehandlingTema.fraOffisiellKode(fagsakInfoOpt.get().getBehandlingstemaOffisiellKode());
        }

        return mapDokumenttype(dokumentTypeId); // Hoveddokumenttype
    }

    private Optional<FagsakInfomasjonDto> fagsakInformasjon(DokumentMetadata metadata) {
        if (metadata.getSaksnummer().isEmpty()) {
            return Optional.empty();
        }
        return fpsakTjeneste.finnFagsakInfomasjon(new SaksnummerDto(metadata.getSaksnummer().orElseThrow()));
    }


    private Destinasjon utledDestinasjonForForsendelse(DokumentMetadata metadata, BehandlingTema behandlingTema,
                                                       Optional<FagsakInfomasjonDto> fagsakInfoOpt) {
        if (metadata.getSaksnummer().isEmpty()) {
            return ruter.bestemDestinasjon(metadata, behandlingTema);
        }

        var saksnummer = metadata.getSaksnummer().orElseThrow();
        if (fagsakInfoOpt.isEmpty() || !erGyldigSaksnummer(fagsakInfoOpt.get(), saksnummer, metadata)) {
            return Destinasjon.GOSYS;
        }

        return new Destinasjon(FPSAK, saksnummer);
    }

    private boolean erGyldigSaksnummer(FagsakInfomasjonDto fagsakInfo, String saksnummer, DokumentMetadata metadata) {
        if (!Objects.equals(fagsakInfo.getAktørId(), metadata.getBrukerId())) { // brukerid == aktørid ved nåværende innseindg. Endre navn/spesifiser.
            LOG.warn("Søkers ID samsvarer ikke med søkers ID i eksisterende sak {}", saksnummer);
            return false;
        }
        return true;
    }

    private OpprettetJournalpost opprettJournalpostFerdigstillHvisSaksnummer(UUID forsendelseId, String aktørId, Destinasjon destinasjon) {
        if (destinasjon.erGosys()) {
            // Midlertidig journalføring, håndteres av fp-mottak.
            // var referanseId = w.getRetryingTask().map(s -> UUID.randomUUID()).Else(forsendelseId); TODO: Fjerne?
            return arkivTjeneste.midlertidigJournalføring(forsendelseId, forsendelseId, aktørId);
        }

        var opprettetJournalpost = arkivTjeneste.forsøkEndeligJournalføring(forsendelseId, aktørId, destinasjon.saksnummer());
        if (!opprettetJournalpost.ferdigstilt()) {
            LOG.info("FORDEL FORSENDELSE kunne ikke ferdigstille sak {} forsendelse {}", destinasjon.saksnummer(), forsendelseId);
        }
        return opprettetJournalpost;
    }

    private void utledNesteSteg(OpprettetJournalpost opprettetJournalpost, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId,
                                UUID forsendelseId, Destinasjon destinasjon) {
        if (!opprettetJournalpost.ferdigstilt() || destinasjon.erGosys() || destinasjon.saksnummer() == null) {
            return; // Ikke ferdigstilt journalpost, eller Gosys - da er det ikke mer å gjøre her.
        }

        var task = ProsessTaskData.forProsessTask(VLKlargjørerTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.setProperty(SAKSNUMMER_PROPERTY, destinasjon.saksnummer());
        task.setProperty(BEHANDLING_TEMA_PROPERTY, behandlingTema.getOffisiellKode());
        task.setProperty(DOKUMENT_TYPE_ID_PROPERTY, dokumentTypeId.getKode());
        taskTjeneste.lagre(task);
    }

}
