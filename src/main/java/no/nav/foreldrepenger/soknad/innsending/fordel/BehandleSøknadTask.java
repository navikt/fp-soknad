package no.nav.foreldrepenger.soknad.innsending.fordel;

import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus.FPSAK;
import static no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivUtil.behandlingtemaFraDokumentType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.Destinasjon;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.DestinasjonsRuter;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.OpprettetJournalpost;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.PdfTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.StrukturertDokumentMapperXML;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
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
        var søknad = orginaleDokumenter.stream().filter(Dokument::erSøknad).findFirst().orElseThrow();
        var xml = strukturertDokumentMapperXML.lagStrukturertDokumentForArkivering(metadata, søknad);
        var pdf = pdfTjeneste.lagPDFFraSøknad(søknad);

        var dokumentTypeId = søknad.getDokumentTypeId();
        var behandlingTema = behandlingtemaFraDokumentType(dokumentTypeId);
        var destinasjon = utledDestinasjonForForsendelse(metadata, søknad, behandlingTema);

        var dokumenterForInnsending = Stream.concat(
            alleVedlegg(orginaleDokumenter),
            Stream.of(xml, pdf))
            .toList();
        var opprettetJournalpost = journalførForsøkEndelig(metadata, dokumenterForInnsending, forsendelseId, destinasjon);

        dokumentRepository.oppdaterForsendelseMetadata(forsendelseId, opprettetJournalpost.journalpostId(), destinasjon);
        utledNesteSteg(opprettetJournalpost, behandlingTema, dokumentTypeId, forsendelseId, destinasjon);
    }

    private static Stream<Dokument> alleVedlegg(List<Dokument> orginaleDokumenter) {
        return orginaleDokumenter.stream().filter(d -> !(ArkivFilType.JSON.equals(d.getArkivFilType()) && d.erSøknad()));
    }

    private Destinasjon utledDestinasjonForForsendelse(DokumentMetadata metadata, Dokument søknad, BehandlingTema behandlingTema) {
        if (metadata.getSaksnummer().isPresent()) {
            return new Destinasjon(FPSAK, metadata.getSaksnummer().orElseThrow());
        }

        return ruter.bestemDestinasjon(metadata, søknad, behandlingTema);
    }

    private OpprettetJournalpost journalførForsøkEndelig(DokumentMetadata metadata, List<Dokument> dokumenter, UUID forsendelseId, Destinasjon destinasjon) {
        if (destinasjon.erGosys()) {
            return arkivTjeneste.midlertidigJournalføring(metadata, dokumenter, forsendelseId); // Midlertidig journalføring, håndteres av fp-mottak.
        }

        var opprettetJournalpost = arkivTjeneste.forsøkEndeligJournalføring(metadata, dokumenter, forsendelseId, destinasjon.saksnummer());
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

        var task = ProsessTaskData.forProsessTask(VLKlargjørerTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.setProperty(SAKSNUMMER_PROPERTY, destinasjon.saksnummer());
        task.setProperty(BEHANDLING_TEMA_PROPERTY, behandlingTema.getOffisiellKode());
        task.setProperty(DOKUMENT_TYPE_ID_PROPERTY, dokumentTypeId.name());
        taskTjeneste.lagre(task);
    }

}
