package no.nav.foreldrepenger.soknad.innsending.fordel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.VLKlargjører;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

import java.time.LocalDateTime;
import java.util.UUID;

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
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(BehandleDokumentforsendelseTask.FORSENDELSE_ID_PROPERTY));
        var saksnummer = prosessTaskData.getPropertyValue(BehandleDokumentforsendelseTask.SAKSNUMMER_PROPERTY);
        var dokumenttypeId = DokumentTypeId.valueOf(prosessTaskData.getPropertyValue(BehandleDokumentforsendelseTask.DOKUMENT_TYPE_ID_PROPERTY));
        var behandlingsTema = BehandlingTema.fraOffisiellKode(prosessTaskData.getPropertyValue(BehandleDokumentforsendelseTask.BEHANDLING_TEMA_PROPERTY));


        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId); // Eller hente fra prosesstask prop?
        var hoverdDokument = dokumentRepository.hentUnikDokument(forsendelseId, true, ArkivFilType.XML); // TODO
        var arkivId = metadata.getArkivId().orElseThrow();
        var jsonPayload = hoverdDokument.map(Dokument::getKlartekstDokument).orElse(null);

        String journalEnhet; // Settes av JournalføringHendelseHåndterer i fpfordel. Alltid null? TODO
        String eksternReferanseId; // Settes av JournalføringHendelseHåndterer i fpfordel. Alltid null? TODO

        klargjører.klargjør(jsonPayload, saksnummer, arkivId, dokumenttypeId, metadata.getForsendelseMottatt(), behandlingsTema, forsendelseId);

        slettForsendelseTask(forsendelseId);
    }

    private void slettForsendelseTask(UUID forsendelseId) {
        var task = ProsessTaskData.forProsessTask(SlettForsendelseTask.class);
        task.setProperty(BehandleDokumentforsendelseTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.setNesteKjøringEtter(LocalDateTime.now().plusHours(2));
        taskTjeneste.lagre(task);
    }
}
