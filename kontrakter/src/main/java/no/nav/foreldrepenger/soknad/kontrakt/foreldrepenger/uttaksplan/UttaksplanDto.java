package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.kontrakter.fpoversikt.FellesUttaksplanDto.UttakPeriodeDto;

public record UttaksplanDto(Boolean ønskerJustertUttakVedFødsel,
                            @NotNull List<@Valid @NotNull Uttaksplanperiode> uttaksperioder,
                            List<@Valid @NotNull UttakPeriodeDto> perioder) {

    public UttaksplanDto(Boolean ønskerJustertUttakVedFødsel, List<Uttaksplanperiode> uttaksperioder) {
        this(ønskerJustertUttakVedFødsel, uttaksperioder, null);
    }
}
