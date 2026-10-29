package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;

/**
 * En endringssøknad inneholder hele uttaksplanen, som lagres i fp-oversikt. FPSAK, PDF og ruting skal bare ha perioder
 * fra og med endringsdato. Valideringen sikrer at endringsdato er lik fom på en periode. Periodene overlapper
 * ikke, så ingen periode krysser endringsdato, og filteret trenger ikke dele perioder.
 * Uten endringsdato (eldre klienter) returneres planen uendret.
 */
public final class EndringsdatoFilter {

    private EndringsdatoFilter() {
    }

    public static List<UttakPeriodeDto> perioderFraEndringsdato(List<UttakPeriodeDto> perioder, LocalDate endringsdato) {
        if (endringsdato == null) {
            return perioder;
        }
        return perioder.stream()
            .filter(p -> !p.fom().isBefore(endringsdato))
            .toList();
    }

    public static UttaksplanDto uttaksplanFraEndringsdato(EndringssøknadForeldrepengerDto endringssøknad) {
        var uttaksplan = endringssøknad.uttaksplan();
        if (endringssøknad.endringsdato() == null) {
            return uttaksplan;
        }
        return new UttaksplanDto(uttaksplan.ønskerJustertUttakVedFødsel(),
            perioderFraEndringsdato(uttaksplan.perioder(), endringssøknad.endringsdato()));
    }

    public static EndringssøknadForeldrepengerDto fraEndringsdato(EndringssøknadForeldrepengerDto e) {
        if (e.endringsdato() == null) {
            return e;
        }
        return new EndringssøknadForeldrepengerDto(e.mottattdato(), e.saksnummer(), e.søkerinfo(), e.rolle(), e.språkkode(), e.barn(),
            e.annenForelder(), uttaksplanFraEndringsdato(e), e.vedlegg(), e.endringsdato());
    }
}
