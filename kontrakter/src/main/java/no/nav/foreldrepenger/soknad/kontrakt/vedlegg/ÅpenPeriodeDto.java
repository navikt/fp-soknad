package no.nav.foreldrepenger.soknad.kontrakt.vedlegg;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ÅpenPeriodeDto(@NotNull LocalDate fom, LocalDate tom) {
}
