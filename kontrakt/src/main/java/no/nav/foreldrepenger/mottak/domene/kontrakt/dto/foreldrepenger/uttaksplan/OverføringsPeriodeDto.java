package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.uttaksplan;

import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.Overføringsårsak;

import java.time.LocalDate;

public record OverføringsPeriodeDto(@NotNull LocalDate fom,
                                    @NotNull LocalDate tom,
                                    @NotNull Overføringsårsak årsak,
                                    @NotNull KontoType konto) implements Uttaksplanperiode {
}
