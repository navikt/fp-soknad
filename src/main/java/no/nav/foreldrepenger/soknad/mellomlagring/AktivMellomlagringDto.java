package no.nav.foreldrepenger.soknad.mellomlagring;

import jakarta.validation.constraints.NotNull;

public record AktivMellomlagringDto(@NotNull boolean engangsstonad,
                                    @NotNull boolean foreldrepenger,
                                    @NotNull boolean svangerskapspenger) {
}
