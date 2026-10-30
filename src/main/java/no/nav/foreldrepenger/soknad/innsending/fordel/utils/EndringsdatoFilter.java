package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import no.nav.foreldrepenger.soknad.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UtsettelseÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;

/**
 * En endringssøknad inneholder hele uttaksplanen, som lagres i fp-oversikt. FPSAK, PDF og ruting skal bare ha perioder
 * fra og med endringsdato. Valideringen sikrer at ingen søkerperiode krysser endringsdato, så filteret deler ikke
 * perioder.
 *
 * FPSAK utleder endringsdato fra første innsendte periode. Starter ingen søkerperiode på endringsdato, for eksempel
 * ved utsatt oppstart, slettet periode eller når søker har fjernet sin del av samtidig uttak, legges det inn fri
 * utsettelse for søker fra endringsdato til dagen før neste søkerperiode. Finnes ingen senere søkerperiode, varer
 * den frie utsettelsen én dag.
 *
 * Uten endringsdato (eldre klienter) returneres planen uendret.
 */
public final class EndringsdatoFilter {

    private EndringsdatoFilter() {
    }

    public static List<UttakPeriodeDto> perioderFraEndringsdato(List<UttakPeriodeDto> perioder, LocalDate endringsdato, BrukerRolle søkerRolle) {
        if (endringsdato == null) {
            return perioder;
        }
        var fraEndringsdato = perioder.stream()
            .filter(p -> !p.fom().isBefore(endringsdato))
            .sorted(Comparator.comparing(UttakPeriodeDto::fom))
            .toList();
        var nesteSøkerperiode = fraEndringsdato.stream()
            .filter(p -> p.søker() != null)
            .map(UttakPeriodeDto::fom)
            .min(LocalDate::compareTo);
        if (nesteSøkerperiode.filter(endringsdato::isEqual).isPresent()) {
            return fraEndringsdato;
        }
        var tom = nesteSøkerperiode.map(d -> d.minusDays(1)).orElse(endringsdato);
        var resultat = new ArrayList<UttakPeriodeDto>();
        resultat.add(friUtsettelse(endringsdato, tom, søkerRolle));
        resultat.addAll(fraEndringsdato);
        return List.copyOf(resultat);
    }

    public static UttaksplanDto uttaksplanFraEndringsdato(EndringssøknadForeldrepengerDto endringssøknad) {
        var uttaksplan = endringssøknad.uttaksplan();
        if (endringssøknad.endringsdato() == null) {
            return uttaksplan;
        }
        return new UttaksplanDto(uttaksplan.ønskerJustertUttakVedFødsel(),
            perioderFraEndringsdato(uttaksplan.perioder(), endringssøknad.endringsdato(), endringssøknad.rolle()));
    }

    public static EndringssøknadForeldrepengerDto fraEndringsdato(EndringssøknadForeldrepengerDto e) {
        if (e.endringsdato() == null) {
            return e;
        }
        return new EndringssøknadForeldrepengerDto(e.mottattdato(), e.saksnummer(), e.søkerinfo(), e.rolle(), e.språkkode(), e.barn(),
            e.annenForelder(), uttaksplanFraEndringsdato(e), e.vedlegg(), e.endringsdato());
    }

    private static UttakPeriodeDto friUtsettelse(LocalDate fom, LocalDate tom, BrukerRolle søkerRolle) {
        var forelder = switch (søkerRolle) {
            case MOR -> Rolle.MOR;
            case FAR, MEDMOR -> Rolle.FAR_MEDMOR;
        };
        var fri = new UttakDto(forelder, null, UtsettelseÅrsak.FRI, null, null, null, null, false, null);
        return new UttakPeriodeDto(fom, tom, fri, null, null);
    }
}
