package no.nav.foreldrepenger.mottak.domene.kontrakt.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record FrilansDto(boolean jobberFremdelesSomFrilans, @NotNull LocalDate oppstart) {
}
