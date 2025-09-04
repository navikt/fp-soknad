package no.nav.foreldrepenger.soknad.innsending.fordel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

import java.util.UUID;

@ApplicationScoped
@ProsessTask(value = "fordeling.slettForsendelse", prioritet = 3)
public class SlettForsendelseTask implements ProsessTaskHandler {

    private DokumentRepository dokumentRepository;

    public SlettForsendelseTask() {
        // CDI
    }

    @Inject
    public SlettForsendelseTask(DokumentRepository dokumentRepository) {
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY));
        var metadata = dokumentRepository.hentUnikDokumentMetadata(forsendelseId);
        if (metadata.flatMap(DokumentMetadata::getJournalpostId).isPresent() && metadata.filter(m -> !ForsendelseStatus.PENDING.equals(m.getStatus()))
                .isPresent()) {
            dokumentRepository.slettForsendelse(forsendelseId);
        }

    }
}
