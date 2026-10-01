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
        var søknadUtenPerioder = søknad(endring, List.of(), null);
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadUtenPerioder))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tom_ny_liste_faller_ikke_tilbake_til_gammel(boolean endring) {
        var søknadMedTomNyListe = søknad(endring, GAMMEL, List.of());
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedTomNyListe))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void ny_liste_er_uavhengig_av_gammel(boolean endring) {
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, UTTAK, null);
        var søknad = søknad(endring, List.of(GAMMEL.getFirst(), GAMMEL.getFirst()), List.of(periode));
        assertThatCode(() -> UttaksperioderValidering.valider(søknad)).doesNotThrowAnyException();
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, null, List.of(periode))))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_overlapp_mellom_søkers_periode_og_opphold(boolean endring) {
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, null, null);
        var annenPart = new UttakPeriodeDto(START, START.plusDays(4), null, UTTAK, null);
        var søknadMedOverlapp = søknad(endring, null, List.of(periode, annenPart));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedOverlapp))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("overlappende");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void plan_med_bare_annen_parts_uttak_godtas_som_opphold(boolean endring) {
        var annenPart = new UttakPeriodeDto(START, START.plusDays(4), null, UTTAK, null);
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, GAMMEL, List.of(annenPart))))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void plan_med_bare_annen_parts_utsettelse_avvises(boolean endring) {
        var utsettelse = new UttakDto(Rolle.FAR_MEDMOR, null, UtsettelseÅrsak.ARBEID, null, null, null, null, false, null);
        var søknadMedKunUtsettelse = søknad(endring, null, List.of(new UttakPeriodeDto(START, START.plusDays(4), null, utsettelse, null)));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedKunUtsettelse))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_ugyldige_datoer_og_overlapp_i_ny_liste(boolean endring) {
        var feilDato = new UttakPeriodeDto(START, START.minusDays(1), UTTAK, null, null);
        var søknadMedFeilDato = søknad(endring, GAMMEL, List.of(feilDato));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedFeilDato))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("tom er før fom");
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, null, null);
        var søknadMedOverlapp = søknad(endring, GAMMEL, List.of(periode, periode));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedOverlapp))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("overlappende");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_for_mange_nye_søkerperioder(boolean endring) {
        var perioder = IntStream.range(0, 201)
            .mapToObj(i -> new UttakPeriodeDto(START.plusDays(i), START.plusDays(i), UTTAK, null, null))
            .toList();
        var søknadMedForMangePerioder = søknad(endring, GAMMEL, perioder);
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedForMangePerioder))
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
