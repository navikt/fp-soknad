package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import java.util.List;

import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

/**
 * Avgjør hvilke perioder i felles uttaksplan som skal sendes til FPSAK: bare søkers egne perioder.
 * Annen parts perioder sendes ikke som opphold. FPSAK fjerner opphold og fri utsettelse som ikke er første periode, og
 * gjør første opphold om til fri utsettelse (OppgittPeriodeRelevans). Ved endringssøknad legger
 * {@link EndringsdatoFilter} inn fri utsettelse på endringsdato når ingen søkerperiode starter der.
 */
public final class FpsakUttaksperioder {

    private FpsakUttaksperioder() {
    }

    public static List<UttakPeriodeDto> søkersPerioder(List<UttakPeriodeDto> perioder) {
        return perioder.stream()
            .filter(p -> p.søker() != null)
            .toList();
    }
}
