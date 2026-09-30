package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

public record UttaksplanDto(Boolean ønskerJustertUttakVedFødsel,
                            List<@Valid @NotNull Uttaksplanperiode> uttaksperioder,
                            List<@Valid @NotNull UttakPeriodeDto> perioder) {

    public UttaksplanDto(Boolean ønskerJustertUttakVedFødsel, List<Uttaksplanperiode> uttaksperioder) {
        this(ønskerJustertUttakVedFødsel, uttaksperioder, null);
    }

    // TODO: Kan slettes i contract fase av ny fellesUttaksplan
    @JsonIgnore
    @AssertTrue(message = "Uttaksplan må ha perioder eller uttaksperioder")
    public boolean isPlanOppgitt() {
        return perioder != null || uttaksperioder != null;
    }
}
