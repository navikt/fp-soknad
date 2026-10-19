package no.nav.foreldrepenger.soknad.innsending.validering;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.ForeldrepengerBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.OverføringÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UtsettelseÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.VedtattResultat;

class UttaksperioderValideringTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 5);
    private static final UttakDto UTTAK = new UttakDto(Rolle.MOR, MØDREKVOTE, null, null, null, null, null, false, null);
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tom_liste_avvises(boolean endring) {
        var søknadMedTomListe = søknad(endring, List.of());
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedTomListe))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void null_liste_avvises(boolean endring) {
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, null)))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("Uttaksplan mangler perioder");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void gyldig_liste_godtas(boolean endring) {
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, UTTAK, null);
        var søknad = søknad(endring, List.of(periode));
        assertThatCode(() -> UttaksperioderValidering.valider(søknad)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_overlapp_mellom_søkers_periode_og_opphold(boolean endring) {
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, null, null);
        var annenPart = new UttakPeriodeDto(START, START.plusDays(4), null, UTTAK, null);
        var søknadMedOverlapp = søknad(endring, List.of(periode, annenPart));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedOverlapp))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("overlappende");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void plan_med_bare_annen_parts_uttak_godtas_som_opphold(boolean endring) {
        var annenPart = new UttakPeriodeDto(START, START.plusDays(4), null, UTTAK, null);
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, List.of(annenPart))))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void plan_med_bare_annen_parts_utsettelse_avvises(boolean endring) {
        var utsettelse = new UttakDto(Rolle.FAR_MEDMOR, null, UtsettelseÅrsak.ARBEID, null, null, null, null, false, null);
        var søknadMedKunUtsettelse = søknad(endring, List.of(new UttakPeriodeDto(START, START.plusDays(4), null, utsettelse, null)));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedKunUtsettelse))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("minst én");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_ugyldige_datoer_og_overlapp_i_ny_liste(boolean endring) {
        var feilDato = new UttakPeriodeDto(START, START.minusDays(1), UTTAK, null, null);
        var søknadMedFeilDato = søknad(endring, List.of(feilDato));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedFeilDato))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("tom er før fom");
        var periode = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, null, null);
        var søknadMedOverlapp = søknad(endring, List.of(periode, periode));
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknadMedOverlapp))
            .isInstanceOf(UttaksperioderValideringException.class).hasMessageContaining("overlappende");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void grense_240_søkerperioder_godtas_og_begge_foreldre_telles_én_gang(boolean endring) {
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, perioder(240, 0, 0))))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_241_søkerperioder(boolean endring) {
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, perioder(241, 0, 0))))
            .isInstanceOf(UttaksperioderValideringException.class)
            .hasMessage("Uttaksplan kan ikke inneholde mer enn 240 søkerperioder (antall: 241)");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void grense_400_perioder_totalt_godtas_før_filtrering(boolean endring) {
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, perioder(240, 80, 80))))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_401_perioder_totalt_før_filtrering(boolean endring) {
        assertThatThrownBy(() -> UttaksperioderValidering.valider(søknad(endring, perioder(240, 80, 81))))
            .isInstanceOf(UttaksperioderValideringException.class)
            .hasMessage("Uttaksplan kan ikke inneholde mer enn 400 perioder totalt (antall: 401)");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void historikk_resultat_og_utsettelse_filtreres_ikke(boolean endring) {
        var avslått = new VedtattResultat(false, false, false, VedtattResultat.Årsak.ANNET);
        var utsettelse = new UttakDto(Rolle.MOR, null, UtsettelseÅrsak.FRI, null, null, null, null, false, avslått);
        var periode = new UttakPeriodeDto(START.minusYears(1), START.minusYears(1).plusDays(4), utsettelse, null, null);
        assertThatCode(() -> UttaksperioderValidering.valider(søknad(endring, List.of(periode))))
            .doesNotThrowAnyException();
    }

    private static SøknadDto søknad(boolean endring, List<UttakPeriodeDto> perioder) {
        return endring
            ? new EndringssøknadBuilder(new Saksnummer("123456")).medUttaksplan(perioder).build()
            : new ForeldrepengerBuilder().medUttaksplan(perioder).build();
    }

    private static List<UttakPeriodeDto> perioder(int antallSøker, int antallOpphold, int antallFiltrerte) {
        var perioder = new ArrayList<UttakPeriodeDto>();
        IntStream.range(0, antallSøker).forEach(i -> perioder.add(søkerperiode(i)));
        IntStream.range(antallSøker, antallSøker + antallOpphold)
            .forEach(i -> perioder.add(new UttakPeriodeDto(START.plusDays(i), START.plusDays(i), null, UTTAK, null)));
        var annenPartsUtsettelse = new UttakDto(Rolle.FAR_MEDMOR, null, UtsettelseÅrsak.ARBEID, null, null, null, null, false, null);
        IntStream.range(antallSøker + antallOpphold, antallSøker + antallOpphold + antallFiltrerte)
            .forEach(i -> perioder.add(new UttakPeriodeDto(START.plusDays(i), START.plusDays(i), null, annenPartsUtsettelse, null)));
        return perioder;
    }

    private static UttakPeriodeDto søkerperiode(int indeks) {
        var dato = START.plusDays(indeks);
        if (indeks == 0) {
            var utsettelse = new UttakDto(Rolle.MOR, null, UtsettelseÅrsak.FRI, null, null, null, null, false, null);
            return new UttakPeriodeDto(dato, dato, utsettelse, null, null);
        }
        if (indeks == 1) {
            var overføring = new UttakDto(Rolle.MOR, MØDREKVOTE, null, OverføringÅrsak.ALENEOMSORG, null, null, null, false, null);
            return new UttakPeriodeDto(dato, dato, overføring, null, null);
        }
        return new UttakPeriodeDto(dato, dato, UTTAK, indeks == 2 ? UTTAK : null, null);
    }
}
