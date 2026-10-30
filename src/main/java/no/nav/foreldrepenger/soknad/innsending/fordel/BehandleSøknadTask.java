package no.nav.foreldrepenger.soknad.innsending.fordel;

import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus.FPSAK;
import static no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivUtil.behandlingstemaFraSøknadDokumentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.Destinasjon;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.DestinasjonsRuter;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.OpprettetJournalpost;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.PdfTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.StrukturertDokumentMapperXML;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "fordeling.behandle.soknad", maxFailedRuns = 4, firstDelay = 10, thenDelay = 30)
public class BehandleSøknadTask implements ProsessTaskHandler {

    public static final String FORSENDELSE_ID_PROPERTY = "forsendelseId";
    public static final String SAKSNUMMER_PROPERTY = "saksnummer";
    public static final String DOKUMENT_TYPE_ID_PROPERTY = "behandlingTema";
    public static final String BEHANDLING_TEMA_PROPERTY = "dokumentTypeId";

    private static final Logger LOG = LoggerFactory.getLogger(BehandleSøknadTask.class);

    private DokumentRepository dokumentRepository;
    private DestinasjonsRuter ruter;
    private ArkivTjeneste arkivTjeneste;
    private ProsessTaskTjeneste taskTjeneste;
    private StrukturertDokumentMapperXML strukturertDokumentMapperXML;
    private PdfTjeneste pdfTjeneste;

    public BehandleSøknadTask() {
        // for CDI
    }

    @Inject
    public BehandleSøknadTask(DokumentRepository dokumentRepository, DestinasjonsRuter ruter, ArkivTjeneste arkivTjeneste, ProsessTaskTjeneste taskTjeneste,
                              StrukturertDokumentMapperXML strukturertDokumentMapperXML, PdfTjeneste pdfTjeneste) {
        this.dokumentRepository = dokumentRepository;
        this.ruter = ruter;
        this.arkivTjeneste = arkivTjeneste;
        this.taskTjeneste = taskTjeneste;
        this.strukturertDokumentMapperXML = strukturertDokumentMapperXML;
        this.pdfTjeneste = pdfTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(FORSENDELSE_ID_PROPERTY));
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var orginaleDokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        var søknad = orginaleDokumenter.stream().filter(DokumentEntitet::erSøknad).findFirst().orElseThrow();
        var xml = strukturertDokumentMapperXML.lagStrukturertDokumentForArkivering(metadata, søknad);
        var pdf = pdfTjeneste.lagPDFFraSøknad(metadata, søknad);

        var dokumentTypeId = søknad.getDokumentTypeId();
        var behandlingTema = behandlingstemaFraSøknadDokumentType(dokumentTypeId);
        var destinasjon = utledDestinasjonForForsendelse(metadata, søknad, behandlingTema);

        var dokumenterForInnsending = Stream.concat(
            alleVedlegg(orginaleDokumenter),
            Stream.of(xml, pdf))
            .toList();
        var opprettetJournalpost = journalførForsøkEndelig(metadata, dokumenterForInnsending, forsendelseId, dokumentTypeId, behandlingTema, destinasjon);

        dokumentRepository.oppdaterForsendelseMetadata(forsendelseId, opprettetJournalpost.journalpostId(), destinasjon);
        utledNesteSteg(opprettetJournalpost, behandlingTema, dokumentTypeId, forsendelseId, destinasjon);
    }

    private static Stream<DokumentEntitet> alleVedlegg(List<DokumentEntitet> orginaleDokumenter) {
        return orginaleDokumenter.stream().filter(d -> !(ArkivFilType.JSON.equals(d.getArkivFilType()) && d.erSøknad()));
    }

    private Destinasjon utledDestinasjonForForsendelse(ForsendelseEntitet metadata, DokumentEntitet søknad, BehandlingTema behandlingTema) {
        if (metadata.getSaksnummer().isPresent()) {
            return new Destinasjon(FPSAK, metadata.getSaksnummer().orElseThrow());
        }

        return ruter.bestemDestinasjon(metadata, søknad, behandlingTema);
    }

    private OpprettetJournalpost journalførForsøkEndelig(ForsendelseEntitet metadata, List<DokumentEntitet> dokumenter, UUID forsendelseId,
                                                         DokumentTypeId dokumentTypeId, BehandlingTema behandlingTema, Destinasjon destinasjon) {
        if (destinasjon.erGosys()) {
            return arkivTjeneste.midlertidigJournalføring(metadata, dokumenter, forsendelseId, dokumentTypeId, behandlingTema); // Midlertidig journalføring, håndteres av fp-mottak.
        }

        var opprettetJournalpost = arkivTjeneste.forsøkEndeligJournalføring(metadata, dokumenter, forsendelseId, destinasjon.saksnummer(), dokumentTypeId, behandlingTema);
        if (!opprettetJournalpost.ferdigstilt()) {
            LOG.info("FP-SOKNAD FORSENDELSE kunne ikke ferdigstille sak {} forsendelse {}", destinasjon.saksnummer(), forsendelseId);
        }
        return opprettetJournalpost;
    }

    private void utledNesteSteg(OpprettetJournalpost opprettetJournalpost, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId,
                                UUID forsendelseId, Destinasjon destinasjon) {
        if (!opprettetJournalpost.ferdigstilt()) {
            return; // Midlertidig journalført, plukkes opp av fp-mottak og avventer handling
        }

        var klargjørTask = ProsessTaskData.forProsessTask(VLKlargjørerTask.class);
        klargjørTask.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        klargjørTask.setProperty(SAKSNUMMER_PROPERTY, destinasjon.saksnummer());
        klargjørTask.setProperty(BEHANDLING_TEMA_PROPERTY, behandlingTema.getOffisiellKode());
        klargjørTask.setProperty(DOKUMENT_TYPE_ID_PROPERTY, dokumentTypeId.name());

        // TODO: Avklar før deploy når forsendelser skal slettes, hvorfor vi venter to timer og når fristen skal starte.
        // Avklar også håndtering av eksisterende tasker og samtidig kjøring av gammel og ny versjon ved deploy/rollback.
        // Gjelder også sletting i BehandleEttersendelseTask.
        var slettTask = ProsessTaskData.forProsessTask(SlettForsendelseTask.class);
        slettTask.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        slettTask.setNesteKjøringEtter(LocalDateTime.now().plusHours(2));

        var gruppe = new ProsessTaskGruppe();
        if (erForeldrepengesøknad(dokumentTypeId)) {
            var oversiktTask = ProsessTaskData.forProsessTask(SendSøknadTilOversiktTask.class);
            oversiktTask.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
            oversiktTask.setProperty(SAKSNUMMER_PROPERTY, destinasjon.saksnummer());
            // Oversikt og fp-sak er uavhengige: oversikt får hele planen, fp-sak bare perioder fra endringsdato.
            // Sletting venter på begge, fordi oversikt leser søknaden fra databasen.
            gruppe.addNesteParallell(oversiktTask, klargjørTask);
        } else {
            gruppe.addNesteSekvensiell(klargjørTask);
        }
        gruppe.addNesteSekvensiell(slettTask);
        taskTjeneste.lagre(gruppe);
    }

    private static boolean erForeldrepengesøknad(DokumentTypeId dokumentTypeId) {
        return switch (dokumentTypeId) {
            case I000002, I000005, I000050 -> true;
            default -> false;
        };
    }

}
