package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record OppholdsPeriodeDto(@NotNull LocalDate fom, @NotNull LocalDate tom, @NotNull Oppholdsårsak årsak) implements Uttaksplanperiode {
}
