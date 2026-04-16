package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UttaksplanDto(Boolean ønskerJustertUttakVedFødsel, @NotNull List<@Valid @NotNull Uttaksplanperiode> uttaksperioder) {
}
