package no.nav.foreldrepenger.mottak.domene.kontrakt.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ÅpenPeriodeDto(@NotNull LocalDate fom, LocalDate tom) {
}