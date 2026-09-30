package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.kontrakter.felles.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.Personoppslag;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.ForeldrepengerBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.UttakplanPeriodeBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
class DestinasjonsRuterTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 5);
    @Mock
    private FpsakTjeneste fpsak;
    @Mock
    private Personoppslag personoppslag;

    @Test
    void bruker_første_nye_søkerperiode_ikke_annen_part_eller_gammel_plan() {
        var uttak = new UttakDto(Rolle.MOR, MØDREKVOTE, null, null, null, null, null, false, null);
        var perioder = List.of(
            new UttakPeriodeDto(START.plusWeeks(1), START.plusWeeks(1), uttak, null, null),
            new UttakPeriodeDto(START.minusWeeks(1), START.minusWeeks(1), null, uttak, null),
            new UttakPeriodeDto(START, START, uttak, null, null));
        when(fpsak.vurderFagsystem(any())).thenReturn(new VurderFagsystemResultat(VurderFagsystemResultat.SendTil.FPSAK, "123456"));

        rute(perioder);

        var captor = ArgumentCaptor.forClass(VurderFagsystemDto.class);
        verify(fpsak).vurderFagsystem(captor.capture());
        assertThat(captor.getValue().getStartDatoForeldrepengerInntektsmelding()).contains(START);
    }

    @Test
    void null_ny_liste_bruker_gammel_plan() {
        when(fpsak.vurderFagsystem(any())).thenReturn(new VurderFagsystemResultat(VurderFagsystemResultat.SendTil.FPSAK, "123456"));
        rute(null);
        var captor = ArgumentCaptor.forClass(VurderFagsystemDto.class);
        verify(fpsak).vurderFagsystem(captor.capture());
        assertThat(captor.getValue().getStartDatoForeldrepengerInntektsmelding()).contains(START.minusMonths(1));
    }

    @Test
    void tom_ny_liste_faller_ikke_tilbake_til_gammel_plan() {
        assertThatThrownBy(() -> rute(List.of())).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mangler perioder til FPSAK");
    }

    @Test
    void endringssøknad_bruker_første_periode_søker_har_gitt_bort_til_annen_part() {
        var uttak = new UttakDto(Rolle.MOR, MØDREKVOTE, null, null, null, null, null, false, null);
        var perioder = List.of(
            new UttakPeriodeDto(START, START, null, uttak, null),
            new UttakPeriodeDto(START.plusWeeks(1), START.plusWeeks(1), uttak, null, null));
        when(fpsak.vurderFagsystem(any())).thenReturn(new VurderFagsystemResultat(VurderFagsystemResultat.SendTil.FPSAK, "123456"));

        var søknad = new EndringssøknadBuilder(new Saksnummer("123456")).medPerioder(perioder).build();
        rute(søknad, DokumentTypeId.I000050);

        var captor = ArgumentCaptor.forClass(VurderFagsystemDto.class);
        verify(fpsak).vurderFagsystem(captor.capture());
        assertThat(captor.getValue().getStartDatoForeldrepengerInntektsmelding()).contains(START);
    }

    private void rute(List<UttakPeriodeDto> perioder) {
        var søknad = new ForeldrepengerBuilder()
            .medUttaksplan(List.of(UttakplanPeriodeBuilder.uttak(MØDREKVOTE, START.minusMonths(1), START.minusDays(1)).build()))
            .medPerioder(perioder).build();
        rute(søknad, DokumentTypeId.I000005);
    }

    private void rute(SøknadDto søknad, DokumentTypeId dokumentTypeId) {
        var id = UUID.randomUUID();
        var metadata = ForsendelseEntitet.builder().setForsendelseId(id).setFødselsnummer("00000000000")
            .setForsendelseMottatt(LocalDateTime.of(2026, 1, 1, 12, 0)).build();
        var dokument = DokumentEntitet.builder().setForsendelseId(id).setDokumentTypeId(dokumentTypeId)
            .setDokumentInnhold(DefaultJsonMapper.getJsonMapper().writeValueAsBytes(søknad), ArkivFilType.JSON).build();
        when(personoppslag.aktørId("00000000000")).thenReturn(new AktørId("123"));
        new DestinasjonsRuter(fpsak, personoppslag).bestemDestinasjon(metadata, dokument, BehandlingTema.FORELDREPENGER_FØDSEL);
    }
}
