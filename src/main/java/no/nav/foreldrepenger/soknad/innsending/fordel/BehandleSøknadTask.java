package no.nav.foreldrepenger.soknad.innsending.fordel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.common.domain.AktørId;
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
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static no.nav.foreldrepenger.common.mapper.DefaultJsonMapper.MAPPER;
import static no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivUtil.behandlingtemaFraDokumentType;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus.FPSAK;

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
    public BehandleSøknadTask(DokumentRepository dokumentRepository, DestinasjonsRuter ruter, ArkivTjeneste arkivTjeneste, ProsessTaskTjeneste taskTjeneste) {
        this.dokumentRepository = dokumentRepository;
        this.ruter = ruter;
        this.arkivTjeneste = arkivTjeneste;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(FORSENDELSE_ID_PROPERTY));

        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var orginaleDokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        var søknad = orginaleDokumenter.stream().filter(Dokument::erSøknad).findFirst().orElseThrow();
        var xml = strukturertDokumentMapperXML.lagStrukturertDokumentForArkivering(søknad, metadata);
        var pdf = pdfTjeneste.lagPDFFraSøknad(søknad);

        // TODO: Aktørid og ikke fødselsnummer!

        var dokumentTypeId = søknad.getDokumentTypeId();
        var behandlingTema = behandlingtemaFraDokumentType(dokumentTypeId);
        var destinasjon = utledDestinasjonForForsendelse(metadata, søknad, behandlingTema);

        var dokumenterForInnsending = Stream.concat(
            orginaleDokumenter.stream().filter(d -> (ArkivFilType.JSON.equals(d.getArkivFilType()) && d.erSøknad())),
            Stream.of(xml, pdf))
            .toList();
        var opprettetJournalpost = journalførForsøkEndelig(metadata, dokumenterForInnsending, forsendelseId, metadata.getBrukerId(), destinasjon);

        dokumentRepository.oppdaterForsendelseMetadata(forsendelseId, opprettetJournalpost.journalpostId(), destinasjon);
        utledNesteSteg(opprettetJournalpost, behandlingTema, dokumentTypeId, forsendelseId, destinasjon);
    }

    private Destinasjon utledDestinasjonForForsendelse(DokumentMetadata metadata, Dokument søknad, BehandlingTema behandlingTema) {
        if (metadata.getSaksnummer().isPresent()) {
            return new Destinasjon(FPSAK, metadata.getSaksnummer().orElseThrow());
        }

        return ruter.bestemDestinasjon(metadata, søknad, behandlingTema);
    }

    private OpprettetJournalpost journalførForsøkEndelig(DokumentMetadata metadata, List<Dokument> dokumenter, UUID forsendelseId,
                                                         String aktørId, Destinasjon destinasjon) {
        if (destinasjon.erGosys()) {
            // Midlertidig journalføring, håndteres av fp-mottak.
            // var referanseId = w.getRetryingTask().map(s -> UUID.randomUUID()).Else(forsendelseId); TODO: Fjerne?
            return arkivTjeneste.midlertidigJournalføring(metadata, dokumenter, forsendelseId, aktørId);
        }

        var opprettetJournalpost = arkivTjeneste.forsøkEndeligJournalføring(metadata, dokumenter, forsendelseId, aktørId, destinasjon.saksnummer());
        if (!opprettetJournalpost.ferdigstilt()) {
            LOG.info("FP-SOKNAD FORSENDELSE kunne ikke ferdigstille sak {} forsendelse {}", destinasjon.saksnummer(), forsendelseId);
        }
        return opprettetJournalpost;
    }

    private void utledNesteSteg(OpprettetJournalpost opprettetJournalpost, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId,
                                UUID forsendelseId, Destinasjon destinasjon) {
        if (!opprettetJournalpost.ferdigstilt()) {
            return; // Midlertidig journalført, avventer handling
        }

        var task = ProsessTaskData.forProsessTask(VLKlargjørerTask.class);
        task.setProperty(FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.setProperty(SAKSNUMMER_PROPERTY, destinasjon.saksnummer());
        task.setProperty(BEHANDLING_TEMA_PROPERTY, behandlingTema.getOffisiellKode());
        task.setProperty(DOKUMENT_TYPE_ID_PROPERTY, dokumentTypeId.name());
        taskTjeneste.lagre(task);
    }

}
