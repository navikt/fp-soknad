package no.nav.foreldrepenger.soknad.kontrakt.opptjening;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record FrilansDto(@NotNull LocalDate oppstart, LocalDate tom) {
}
