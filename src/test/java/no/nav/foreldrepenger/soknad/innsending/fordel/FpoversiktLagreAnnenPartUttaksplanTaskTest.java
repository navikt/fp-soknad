package no.nav.foreldrepenger.soknad.innsending.fordel;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle.FAR_MEDMOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.FpoversiktTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.FpoversiktUttaksplanRequest;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
class FpoversiktLagreAnnenPartUttaksplanTaskTest {

    private static final LocalDateTime MOTTATT = LocalDateTime.of(2026, 1, 1, 12, 30);

    @Mock
    private DokumentRepository dokumentRepository;
    @Mock
    private FpoversiktTjeneste fpoversiktTjeneste;
    @Mock
    private ProsessTaskTjeneste taskTjeneste;
    @Mock
    private ForsendelseEntitet metadata;

    private FpoversiktLagreAnnenPartUttaksplanTask task;
    private ProsessTaskData taskData;

    @BeforeEach
    void setUp() {
        task = new FpoversiktLagreAnnenPartUttaksplanTask(dokumentRepository, fpoversiktTjeneste, taskTjeneste);
        var forsendelseId = UUID.randomUUID();
        taskData = ProsessTaskData.forProsessTask(FpoversiktLagreAnnenPartUttaksplanTask.class);
        taskData.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        taskData.setProperty(BehandleSøknadTask.SAKSNUMMER_PROPERTY, "123");
        taskData.setProperty(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY, "ab0326");
        taskData.setProperty(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY, DokumentTypeId.I000005.name());
        taskData.setGruppe("søker");
        taskData.setSekvens("123");

        when(dokumentRepository.hentEksaktDokumentMetadata(forsendelseId)).thenReturn(metadata);
        when(metadata.getForsendelseMottatt()).thenReturn(MOTTATT);
        when(dokumentRepository.hentSøknadDokument(forsendelseId)).thenReturn(Optional.of(søknadDokument(forsendelseId)));
    }

    @Test
    void lagrer_i_fpoversikt_før_vl_klargjøring_planlegges() {
        task.doTask(taskData);

        var rekkefølge = inOrder(fpoversiktTjeneste, taskTjeneste);
        rekkefølge.verify(fpoversiktTjeneste).lagreAnnenPartsUttaksplan(org.mockito.ArgumentMatchers.any());
        rekkefølge.verify(taskTjeneste).lagre(org.mockito.ArgumentMatchers.any(ProsessTaskData.class));

        var requestCaptor = ArgumentCaptor.forClass(FpoversiktUttaksplanRequest.class);
        verify(fpoversiktTjeneste).lagreAnnenPartsUttaksplan(requestCaptor.capture());
        assertThat(requestCaptor.getValue().saksnummer()).isEqualTo("123");
        assertThat(requestCaptor.getValue().mottattTidspunkt()).isEqualTo(MOTTATT);
        assertThat(requestCaptor.getValue().perioder()).hasSize(1);

        var taskCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste).lagre(taskCaptor.capture());
        assertThat(taskCaptor.getValue().taskType().value()).isEqualTo("fordeling.klargjoering");
        assertThat(taskCaptor.getValue().getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY))
            .isEqualTo(taskData.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY));
        assertThat(taskCaptor.getValue().getPropertyValue(BehandleSøknadTask.SAKSNUMMER_PROPERTY)).isEqualTo("123");
        assertThat(taskCaptor.getValue().getPropertyValue(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY)).isEqualTo("ab0326");
        assertThat(taskCaptor.getValue().getPropertyValue(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY))
            .isEqualTo(DokumentTypeId.I000005.name());
        assertThat(taskCaptor.getValue().getGruppe()).isEqualTo("søker");
        assertThat(taskCaptor.getValue().getSekvens()).isEqualTo("123");
    }

    @Test
    void planlegger_ikke_vl_klargjøring_når_fpoversikt_feiler() {
        var feil = new RuntimeException("fpoversikt utilgjengelig");
        org.mockito.Mockito.doThrow(feil).when(fpoversiktTjeneste)
            .lagreAnnenPartsUttaksplan(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> task.doTask(taskData)).isSameAs(feil);

        verify(taskTjeneste, never()).lagre(org.mockito.ArgumentMatchers.any(ProsessTaskData.class));
    }

    private static DokumentEntitet søknadDokument(UUID forsendelseId) {
        var start = LocalDate.of(2026, 1, 5);
        var annenPart = new UttakDto(FAR_MEDMOR, FELLESPERIODE, null, null, null, null, null, false, null);
        var fellesPlan = new FellesUttaksplanDto(start, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE,
            List.of(new UttakPeriodeDto(start, start.plusDays(4), null, annenPart, null)));
        var søknad = new ForeldrepengesøknadDto(null, null, null, null, null, null, null, List.of(), null, Dekningsgrad.HUNDRE, null,
            fellesPlan, List.of(), List.of());
        return DokumentEntitet.builder()
            .setDokumentInnhold(DefaultJsonMapper.getJsonMapper().writeValueAsBytes(søknad), ArkivFilType.JSON)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000005)
            .build();
    }
}
