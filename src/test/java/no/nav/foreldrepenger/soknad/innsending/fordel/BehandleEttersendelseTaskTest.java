package no.nav.foreldrepenger.soknad.innsending.fordel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.soknad.database.JpaExtension;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.OpprettetJournalpost;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.DokgenRestKlient;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.PdfTjeneste;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;


@ExtendWith(MockitoExtension.class)
@ExtendWith(JpaExtension.class)
class BehandleEttersendelseTaskTest {

    @Mock
    private ArkivTjeneste arkivtjeneste;
    @Mock
    private ProsessTaskTjeneste taskTjeneste;
    @Mock
    private DokgenRestKlient dokgenRestKlient;
    @Mock
    private FpsakTjeneste fpsakTjeneste;

    private DokumentRepository dokumentRepository;
    private BehandleEttersendelseTask task;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        dokumentRepository = new DokumentRepository(entityManager);
        var pdfTjeneste = new PdfTjeneste(dokgenRestKlient, dokumentRepository);
        task = new BehandleEttersendelseTask(dokumentRepository, fpsakTjeneste, arkivtjeneste, taskTjeneste, pdfTjeneste);
    }

    @Test
    void ettersendelse_av_vedlegg_test() {
        // Arrange
        var forsendelseId = UUID.randomUUID();
        var saksnummer = "123456";
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer("123456789")
            .setSaksnummer(saksnummer)
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        dokumentRepository.lagre(metadata);

        var vedlegg1 = DokumentEntitet.builder()
            .setDokumentInnhold(new byte[]{1, 2, 3}, ArkivFilType.PDFA)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000051)
            .build();
        dokumentRepository.lagre(vedlegg1);

        var vedlegg2 = DokumentEntitet.builder()
            .setDokumentInnhold(new byte[]{4, 5, 6, 7, 8, 9}, ArkivFilType.PDFA)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000132)
            .build();
        dokumentRepository.lagre(vedlegg2);

        when(fpsakTjeneste.finnFagsakInfomasjon(any())).thenReturn(java.util.Optional.of(new FagsakInfomasjonDto("99123456789", BehandlingTema.FORELDREPENGER_FØDSEL.getOffisiellKode())));
        when(arkivtjeneste.forsøkEndeligJournalføring(any(), any(), any(), any(), any(), any())).thenReturn(new OpprettetJournalpost("9999999", true));

        // Act
        var prosessTaskData = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        prosessTaskData.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.doTask(prosessTaskData);

        // Assert
        var forsendelse = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(forsendelse.getSaksnummer()).hasValue(saksnummer);
        assertThat(forsendelse.getJournalpostId()).hasValue("9999999");

        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        assertThat(dokumenter).hasSize(2);
        assertThat(dokumenter).extracting(DokumentEntitet::getByteArrayDokument).containsExactlyInAnyOrder(vedlegg1.getByteArrayDokument(), vedlegg2.getByteArrayDokument());
        assertThat(dokumenter).extracting(DokumentEntitet::getArkivFilType).containsExactlyInAnyOrder(vedlegg1.getArkivFilType(), vedlegg2.getArkivFilType());

        verify(arkivtjeneste, times(1)).forsøkEndeligJournalføring(any(), eq(dokumenter), any(), any(), any(), any());

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var vlklargjørtask = captor.getValue();
        assertThat(vlklargjørtask.taskType().value()).isEqualTo("fordeling.klargjoering");
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY)).isEqualTo(forsendelseId.toString());
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.SAKSNUMMER_PROPERTY)).isEqualTo(saksnummer);
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY)).isEqualTo(BehandlingTema.FORELDREPENGER_FØDSEL.getOffisiellKode());
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY)).isEqualTo(DokumentTypeId.I000051.getKode());

    }
}
