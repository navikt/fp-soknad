package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.soknad.database.JpaExtension;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JpaExtension.class)
class DokumentRepositoryTest {

    private DokumentRepository dokumentRepository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        dokumentRepository = new DokumentRepository(entityManager);
    }

    @Test
    void skal_oppdatere_soknad_json_for_eksisterende_forsendelse() {
        var forsendelseId = UUID.randomUUID();
        var opprinneligJson = "{\"original\":true}";
        var korrigertJson = "{\"korrigert\":true}";
        lagreForsendelseMedSoknad(forsendelseId, opprinneligJson.getBytes(UTF_8), ArkivFilType.JSON);
        var soknadDokument = dokumentRepository.hentSøknadDokument(forsendelseId).orElseThrow();

        dokumentRepository.oppdaterSøknadJson(soknadDokument, korrigertJson.getBytes(UTF_8));

        assertThat(soknadDokument.getKlartekstDokument()).isEqualTo(korrigertJson);
    }

    @Test
    void hentSøknadDokument_skal_ikke_hente_ikke_json_dokument() {
        var forsendelseId = UUID.randomUUID();
        lagreForsendelseMedSoknad(forsendelseId, "{}".getBytes(UTF_8), ArkivFilType.XML);

        assertThat(dokumentRepository.hentSøknadDokument(forsendelseId)).isEmpty();
    }

    private void lagreForsendelseMedSoknad(UUID forsendelseId, byte[] dokumentBytes, ArkivFilType arkivFilType) {
        var forsendelse = ForsendelseEntitet.builder()
            .setFødselsnummer("12345678901")
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        dokumentRepository.lagre(forsendelse);

        var soknadDokument = DokumentEntitet.builder()
            .setDokumentInnhold(dokumentBytes, arkivFilType)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000005)
            .build();
        dokumentRepository.lagre(soknadDokument);
    }
}

