package no.nav.foreldrepenger.soknad.kontrakt.vedlegg;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.soknad.kontrakt.svangerskapspenger.ArbeidsforholdDto;

public record Dokumenterer(@NotNull DokumentererType type,
                           @Valid ArbeidsforholdDto arbeidsforhold,
                           @Size(max = 200) List<@Valid @NotNull ÅpenPeriodeDto> perioder) {
        public enum DokumentererType {
            BARN,
            OPPTJENING,
            UTTAK,
            TILRETTELEGGING,
        }
    }
