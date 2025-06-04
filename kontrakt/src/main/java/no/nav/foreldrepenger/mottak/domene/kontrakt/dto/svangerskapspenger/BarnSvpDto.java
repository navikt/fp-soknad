package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.svangerskapspenger;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BarnSvpDto(@NotNull LocalDate termindato, LocalDate fødselsdato) {
}
