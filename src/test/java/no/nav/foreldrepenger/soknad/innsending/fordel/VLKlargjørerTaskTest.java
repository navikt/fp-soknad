package no.nav.foreldrepenger.soknad.innsending.fordel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.soknad.database.JpaExtension;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.VLKlargjører;
import no.nav.foreldrepenger.soknad.innsending.fordel.fptilbake.FtilbakeTjeneste;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JpaExtension.class)
class VLKlargjørerTaskTest {

    @Mock
    private FpsakTjeneste fpsakTjeneste;
    @Mock
    private FtilbakeTjeneste fptilbakeTjeneste;
    @Mock
    private ProsessTaskTjeneste taskTjeneste;

    private DokumentRepository dokumentRepository;
    private VLKlargjørerTask task;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        dokumentRepository = new DokumentRepository(entityManager);
        var vlKlargjører = new VLKlargjører(fpsakTjeneste, fptilbakeTjeneste);
        task = new VLKlargjørerTask(dokumentRepository, vlKlargjører, taskTjeneste);
    }

    @Test
    void verifiser_forsendelse_mot_fpsak_fptilbake_og_sletting_av_forsendlse_ved_ok() {
        var forsendelseId = UUID.randomUUID();
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer("123456789")
            .setSaksnummer("123")
            .setForsendelseId(forsendelseId)
            .setStatus(ForsendelseStatus.FPSAK)
            .setJournalpostId("9999")
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        dokumentRepository.lagre(metadata);

        var søknadXML = DokumentEntitet.builder()
            .setDokumentInnhold("Dette er en XML".getBytes(StandardCharsets.UTF_8), ArkivFilType.XML)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000005)
            .build();
        dokumentRepository.lagre(søknadXML);

        var behandlingTema = BehandlingTema.FORELDREPENGER_ENDRING;
        var prosessTaskData = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        prosessTaskData.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        prosessTaskData.setProperty(BehandleSøknadTask.SAKSNUMMER_PROPERTY, metadata.getSaksnummer().orElseThrow());
        prosessTaskData.setProperty(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY, DokumentTypeId.I000005.getKode());
        prosessTaskData.setProperty(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY, behandlingTema.getOffisiellKode());

        task.doTask(prosessTaskData);

        var captorFpsak = ArgumentCaptor.forClass(JournalpostMottakDto.class);
        verify(fpsakTjeneste, times(1)).sendOgKnyttJournalpost(captorFpsak.capture());
        verify(fptilbakeTjeneste, never()).send(any());
        var journalpostMottakDtoFpsak = captorFpsak.getValue();
        assertThat(journalpostMottakDtoFpsak.getForsendelseId()).hasValue(forsendelseId);
        assertThat(journalpostMottakDtoFpsak.getSaksnummer()).isEqualTo(metadata.getSaksnummer().orElseThrow());
        assertThat(journalpostMottakDtoFpsak.getJournalpostId()).isEqualTo(metadata.getJournalpostId().orElseThrow());
        assertThat(journalpostMottakDtoFpsak.getBehandlingstemaOffisiellKode()).isEqualTo(behandlingTema.getOffisiellKode());
        assertThat(journalpostMottakDtoFpsak.getDokumentTypeIdOffisiellKode()).hasValue(søknadXML.getDokumentTypeId().getKode());assertThat(journalpostMottakDtoFpsak.getPayloadXml()).hasValue(søknadXML.getKlartekstDokument());


        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var slettForsendelseTask = captor.getValue();
        assertThat(slettForsendelseTask.taskType().value()).isEqualTo("fordeling.slettForsendelse");
        assertThat(slettForsendelseTask.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY)).isEqualTo(forsendelseId.toString());
        assertThat(slettForsendelseTask.getNesteKjøringEtter()).isAfter(LocalDateTime.now().plusHours(1));
    }

    @Test
    void verifiser_forsendelse_sendes_til_fptilbake_ved_uttalelse_om_tilbakekreving_og_sletting_av_forsendelse() {
        var forsendelseId = UUID.randomUUID();
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer("123456789")
            .setSaksnummer("123")
            .setForsendelseId(forsendelseId)
            .setStatus(ForsendelseStatus.FPSAK)
            .setJournalpostId("9999")
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        dokumentRepository.lagre(metadata);

        var uttalelseTilbakekreving = DokumentEntitet.builder()
            .setDokumentInnhold(new byte[]{1,2,3,4}, ArkivFilType.JSON)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000114)
            .build();
        dokumentRepository.lagre(uttalelseTilbakekreving);

        var behandlingTema = BehandlingTema.FORELDREPENGER_ENDRING;
        var prosessTaskData = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        prosessTaskData.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        prosessTaskData.setProperty(BehandleSøknadTask.SAKSNUMMER_PROPERTY, metadata.getSaksnummer().orElseThrow());
        prosessTaskData.setProperty(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY, DokumentTypeId.I000114.getKode());
        prosessTaskData.setProperty(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY, behandlingTema.getOffisiellKode());

        task.doTask(prosessTaskData);

        var captorFptilbake = ArgumentCaptor.forClass(JournalpostMottakDto.class);
        verify(fpsakTjeneste, never()).sendOgKnyttJournalpost(any());
        verify(fptilbakeTjeneste, times(1)).send(captorFptilbake.capture());
        var journalpostMottakDtoFptilbake = captorFptilbake.getValue();
        assertThat(journalpostMottakDtoFptilbake.getForsendelseId()).hasValue(forsendelseId);
        assertThat(journalpostMottakDtoFptilbake.getSaksnummer()).isEqualTo(metadata.getSaksnummer().orElseThrow());
        assertThat(journalpostMottakDtoFptilbake.getJournalpostId()).isEqualTo(metadata.getJournalpostId().orElseThrow());
        assertThat(journalpostMottakDtoFptilbake.getBehandlingstemaOffisiellKode()).isEqualTo(behandlingTema.getOffisiellKode());
        assertThat(journalpostMottakDtoFptilbake.getDokumentTypeIdOffisiellKode()).hasValue(uttalelseTilbakekreving.getDokumentTypeId().getKode());

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var slettForsendelseTask = captor.getValue();
        assertThat(slettForsendelseTask.taskType().value()).isEqualTo("fordeling.slettForsendelse");
        assertThat(slettForsendelseTask.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY)).isEqualTo(forsendelseId.toString());
        assertThat(slettForsendelseTask.getNesteKjøringEtter()).isAfter(LocalDateTime.now().plusHours(1));
    }
}
