package no.nav.foreldrepenger.soknad.innsending.fordel;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.soknad.database.JpaExtension;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JpaExtension.class)
class SlettForsendelseTaskTest {

    private DokumentRepository dokumentRepository;
    private SlettForsendelseTask task;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        dokumentRepository = new DokumentRepository(entityManager);
        task = new SlettForsendelseTask(dokumentRepository);
    }

    @Test
    void forsendsle_med_dokumenter_slettes_men_bare_med_samme_forsendelse_id() {
        var forsendelseId1 = UUID.randomUUID();
        lagForsendelse(forsendelseId1);

        var forsendelseId2 = UUID.randomUUID();
        lagForsendelse(forsendelseId2);

        assertThat(dokumentRepository.hentDokumenter(forsendelseId1)).hasSize(2);
        assertThat(dokumentRepository.hentUnikDokumentMetadata(forsendelseId1)).isPresent();

        var prosessTaskData = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        prosessTaskData.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId1.toString());

        task.doTask(prosessTaskData);

        assertThat(dokumentRepository.hentDokumenter(forsendelseId1)).isEmpty();
        assertThat(dokumentRepository.hentUnikDokumentMetadata(forsendelseId1)).isEmpty();
        assertThat(dokumentRepository.hentDokumenter(forsendelseId2)).hasSize(2);
        assertThat(dokumentRepository.hentUnikDokumentMetadata(forsendelseId2)).isPresent();
    }

    private void lagForsendelse(UUID forsendelseID) {
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer("123456789")
            .setSaksnummer("123")
            .setForsendelseId(forsendelseID)
            .setStatus(ForsendelseStatus.FPSAK)
            .setJournalpostId("9999")
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        dokumentRepository.lagre(metadata);

        var søknadDokument = DokumentEntitet.builder()
            .setDokumentInnhold("Ett eller anna innhold".getBytes(StandardCharsets.UTF_8), ArkivFilType.JSON)
            .setForsendelseId(forsendelseID)
            .setDokumentTypeId(DokumentTypeId.I000005)
            .build();
        dokumentRepository.lagre(søknadDokument);

        var søknadXML = DokumentEntitet.builder()
            .setDokumentInnhold("Dette er en XML".getBytes(StandardCharsets.UTF_8), ArkivFilType.XML)
            .setForsendelseId(forsendelseID)
            .setDokumentTypeId(DokumentTypeId.I000005)
            .build();
        dokumentRepository.lagre(søknadXML);
    }
}
