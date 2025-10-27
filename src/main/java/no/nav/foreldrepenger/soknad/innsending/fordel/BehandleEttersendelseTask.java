package no.nav.foreldrepenger.soknad.innsending.fordel;

import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus.FPSAK;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.vedlegg.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.Destinasjon;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.OpprettetJournalpost;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.PdfTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "fordeling.behandle.ettersendelse", maxFailedRuns = 4, firstDelay = 10, thenDelay = 30)
public class BehandleEttersendelseTask implements ProsessTaskHandler {

    public static final String FORSENDELSE_ID_PROPERTY = "forsendelseId";
    public static final String SAKSNUMMER_PROPERTY = "saksnummer";
    public static final String DOKUMENT_TYPE_ID_PROPERTY = "behandlingTema";
    public static final String BEHANDLING_TEMA_PROPERTY = "dokumentTypeId";

    private DokumentRepository dokumentRepository;
    private FpsakTjeneste fpsakTjeneste;
    private ArkivTjeneste arkivTjeneste;
    private ProsessTaskTjeneste taskTjeneste;
    private PdfTjeneste pdfTjeneste;

    public BehandleEttersendelseTask() {
        // for CDI
    }

    @Inject
    public BehandleEttersendelseTask(DokumentRepository dokumentRepository, FpsakTjeneste fpsakTjeneste, ArkivTjeneste arkivTjeneste,
                                     ProsessTaskTjeneste taskTjeneste, PdfTjeneste pdfTjeneste) {
        this.dokumentRepository = dokumentRepository;
        this.fpsakTjeneste = fpsakTjeneste;
        this.arkivTjeneste = arkivTjeneste;
        this.taskTjeneste = taskTjeneste;
        this.pdfTjeneste = pdfTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(FORSENDELSE_ID_PROPERTY));

        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var hovedDokumenttype = utledHovedDokumentType(dokumenter);
        var behandlingTema = utledBehandlingstema(metadata);
        var destinasjon = new Destinasjon(FPSAK, metadata.getSaksnummer().orElseThrow());

        var uttalelseOmTilbakekrevingPDF = dokumenter.stream()
            .filter(DokumentEntitet::erUttalelseOmTilbakebetaling)
            .findFirst()
            .map(d -> pdfTjeneste.lagUttalelseOmTilbakebetalingPDF(metadata, d));

        var dokumenterForJournalføring = uttalelseOmTilbakekrevingPDF.isPresent()
            ? Stream.concat(uttalelseOmTilbakekrevingPDF.stream(), dokumenter.stream().filter(dokument -> !dokument.erUttalelseOmTilbakebetaling())).toList()
            : dokumenter;

        var opprettetJournalpost = arkivTjeneste.forsøkEndeligJournalføring(metadata, dokumenterForJournalføring, forsendelseId, destinasjon.saksnummer(), hovedDokumenttype,
            behandlingTema);
        dokumentRepository.oppdaterForsendelseMetadata(forsendelseId, opprettetJournalpost.journalpostId(), destinasjon);
        utledNesteSteg(opprettetJournalpost, behandlingTema, hovedDokumenttype, forsendelseId, destinasjon);
    }

    // Er konsekvent og velger en hoveddokumenttype for forsendelsen (gitt flere type vedlegg)
    private static DokumentTypeId utledHovedDokumentType(List<DokumentEntitet> dokumenter) {
        return dokumenter.stream()
            .map(DokumentEntitet::getDokumentTypeId)
            .filter(t -> !DokumentTypeId.I000060.equals(t))
            .min(Comparator.comparing(DokumentTypeId::getKode))
            .orElse(DokumentTypeId.I000060);
    }

    private BehandlingTema utledBehandlingstema(ForsendelseEntitet metadata) {
        var fagsakinfo =  fpsakTjeneste.finnFagsakInfomasjon(new SaksnummerDto(metadata.getSaksnummer().orElseThrow())).orElseThrow();
        return BehandlingTema.fraOffisiellKode(fagsakinfo.behandlingstemaOffisiellKode());
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
