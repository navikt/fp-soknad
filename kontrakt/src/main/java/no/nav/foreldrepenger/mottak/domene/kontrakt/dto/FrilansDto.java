package no.nav.foreldrepenger.mottak.domene.kontrakt.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FrilansDto(boolean jobberFremdelesSomFrilans, @NotNull LocalDate oppstart) {
}
