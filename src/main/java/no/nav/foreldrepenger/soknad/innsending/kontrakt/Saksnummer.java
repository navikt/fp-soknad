package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record Saksnummer(@JsonValue @NotNull @Digits(integer = 18, fraction = 0) String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Saksnummer { // NOSONAR
    }
}
