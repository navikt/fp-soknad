package no.nav.foreldrepenger.soknad.innsending.validering;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.ForeldrepengerBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.UttakplanPeriodeBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UtsettelseÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.VedtattResultat;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;

class UttaksperioderValideringTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 5);
    private static final UttakDto UTTAK = new UttakDto(Rolle.MOR, MØDREKVOTE, null, null, null, null, null, false, null);
    private static final List<Uttaksplanperiode> GAMMEL = List.of(
        UttakplanPeriodeBuilder.uttak(MØDREKVOTE, START, START.plusDays(4)).build());

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bruker_gammel_liste_når_ny_er_null(boolean endring) {
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, GAMMEL, null))).doesNotThrowAnyException();
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, List.of(), null)))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tom_ny_liste_faller_ikke_tilbake_til_gammel(boolean endring) {
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, GAMMEL, List.of())))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void ny_liste_er_uavhengig_av_gammel_og_tillater_overlapp_med_annen_part(boolean endring) {
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, null, null);
        var annenPart = new UttakPeriodeDto(START, START.plusDays(4), null, UTTAK, null);
        var søknad = søknad(endring, List.of(GAMMEL.getFirst(), GAMMEL.getFirst()), List.of(periode, annenPart));
        assertThatCode(() -> UttaksperioderValidering.valider(søknad)).doesNotThrowAnyException();
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, null, List.of(periode))))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void annen_parts_perioder_erstatter_ikke_søkers_perioder(boolean endring) {
        var annenPart = new UttakPeriodeDto(START, START.plusDays(4), null, UTTAK, null);
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, GAMMEL, List.of(annenPart))))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_ugyldige_datoer_og_overlapp_i_ny_liste(boolean endring) {
        var feilDato = new UttakPeriodeDto(START, START.minusDays(1), UTTAK, null, null);
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, GAMMEL, List.of(feilDato))))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("tom er før fom");
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, null, null);
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, GAMMEL, List.of(periode, periode))))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("overlappende");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_for_mange_nye_søkerperioder(boolean endring) {
        var perioder = IntStream.range(0, 201)
            .mapToObj(i -> new UttakPeriodeDto(START.plusDays(i), START.plusDays(i), UTTAK, null, null))
            .toList();
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, GAMMEL, perioder)))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("200");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void historikk_resultat_og_utsettelse_filtreres_ikke(boolean endring) {
        var avslått = new VedtattResultat(false, false, false, VedtattResultat.Årsak.ANNET);
        var utsettelse = new UttakDto(Rolle.MOR, null, UtsettelseÅrsak.FRI, null, null, null, null, false, avslått);
        var periode = new UttakPeriodeDto(START.minusYears(1), START.minusYears(1).plusDays(4), utsettelse, null, null);
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, null, List.of(periode))))
            .doesNotThrowAnyException();
    }

    private static SøknadDto søknad(boolean endring, List<Uttaksplanperiode> gamle, List<UttakPeriodeDto> nye) {
        return endring
            ? new EndringssøknadBuilder(new Saksnummer("123456")).medUttaksplan(gamle).medPerioder(nye).build()
            : new ForeldrepengerBuilder().medUttaksplan(gamle).medPerioder(nye).build();
    }
}
