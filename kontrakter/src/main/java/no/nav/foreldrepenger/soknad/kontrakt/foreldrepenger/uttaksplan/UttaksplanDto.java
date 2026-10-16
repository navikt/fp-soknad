package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

public record UttaksplanDto(Boolean ønskerJustertUttakVedFødsel,
                            @NotNull List<@Valid @NotNull UttakPeriodeDto> perioder) {
}
