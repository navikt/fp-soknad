package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import static no.nav.vedtak.util.InputValideringRegex.FRITEKST;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record Fødselsnummer(@Pattern(regexp = FRITEKST) @NotNull @JsonValue String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Fødselsnummer { // NOSONAR
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [fnr=******]";
    }
}
