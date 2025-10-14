package no.nav.foreldrepenger.soknad.innsending.fordel;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.VLKlargjører;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

/*
 * Sender dokument til fpsak og evt til fptilbake
 */
@ApplicationScoped
@ProsessTask(value = "fordeling.klargjoering", maxFailedRuns = 4, firstDelay = 10, thenDelay = 30)
public class VLKlargjørerTask implements ProsessTaskHandler {

    private DokumentRepository dokumentRepository;
    private VLKlargjører klargjører;
    private ProsessTaskTjeneste taskTjeneste;

    public VLKlargjørerTask() {
        // CDI
    }

    @Inject
    public VLKlargjørerTask(DokumentRepository dokumentRepository, VLKlargjører klargjører, ProsessTaskTjeneste taskTjeneste) {
        this.dokumentRepository = dokumentRepository;
        this.klargjører = klargjører;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY));
        var saksnummer = prosessTaskData.getPropertyValue(BehandleSøknadTask.SAKSNUMMER_PROPERTY);
        var dokumenttypeId = DokumentTypeId.valueOf(prosessTaskData.getPropertyValue(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY));
        var behandlingsTema = BehandlingTema.fraOffisiellKode(prosessTaskData.getPropertyValue(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY));

        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId); // Eller hente fra prosesstask prop?
        var hoverdDokument = dokumentRepository.hentDokumenter(forsendelseId, ArkivFilType.XML).stream()
            .filter(DokumentEntitet::erSøknad)
            .findFirst();
        var arkivId = metadata.getJournalpostId().orElseThrow();
        var xml = hoverdDokument.map(DokumentEntitet::getKlartekstDokument).orElse(null);

        klargjører.klargjør(xml, saksnummer, arkivId, dokumenttypeId, metadata.getForsendelseMottatt(), behandlingsTema, forsendelseId);

        slettForsendelseTask(forsendelseId);
    }

    private void slettForsendelseTask(UUID forsendelseId) {
        var task = ProsessTaskData.forProsessTask(SlettForsendelseTask.class);
        task.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.setNesteKjøringEtter(LocalDateTime.now().plusHours(2));
        taskTjeneste.lagre(task);
    }
}
