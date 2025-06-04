package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.uttaksplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UttaksplanDto(Boolean ønskerJustertUttakVedFødsel, @Valid @Size(min = 1, max = 200) List<@Valid @NotNull Uttaksplanperiode> uttaksperioder) {
}
