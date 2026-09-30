package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import java.util.List;

import no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

/**
 * Avgjør hvilke perioder i felles uttaksplan som skal sendes til FPSAK.
 * Søkers egne perioder sendes alltid. Ved endringssøknad sendes i tillegg perioder der bare annen part har uttak
 * som opphold, slik at FPSAK får riktig endringsdato og fjerner søkers tidligere uttak i perioden.
 */
public final class FpsakUttaksperioder {

    private FpsakUttaksperioder() {
    }

    public static List<UttakPeriodeDto> perioderTilFpsak(List<UttakPeriodeDto> perioder, boolean erEndringssøknad) {
        return perioder.stream()
            .filter(p -> p.søker() != null || (erEndringssøknad && erOppholdsperiode(p)))
            .toList();
    }

    public static boolean erOppholdsperiode(UttakPeriodeDto periode) {
        if (periode.søker() != null || periode.annenPart() == null) {
            return false;
        }
        var kontoType = periode.annenPart().kontoType();
        return kontoType != null && kontoType != KontoType.FORELDREPENGER_FØR_FØDSEL;
    }
}
