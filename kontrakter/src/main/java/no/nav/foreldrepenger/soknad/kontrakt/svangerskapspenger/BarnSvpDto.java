package no.nav.foreldrepenger.soknad.kontrakt.svangerskapspenger;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record BarnSvpDto(@NotNull LocalDate termindato, LocalDate fødselsdato) {
}
