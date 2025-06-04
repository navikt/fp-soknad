package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.uttaksplan;

import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.Oppholdsårsak;

import java.time.LocalDate;

public record OppholdsPeriodeDto(@NotNull LocalDate fom,
                                 @NotNull LocalDate tom,
                                 @NotNull Oppholdsårsak årsak) implements Uttaksplanperiode {
}
