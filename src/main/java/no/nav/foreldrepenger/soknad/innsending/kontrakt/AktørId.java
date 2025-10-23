package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.Pattern;


public record AktørId(@JsonValue @Pattern(regexp = "^[\\p{Digit}]*$") String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public AktørId { // NOSONAR
    }

    @Override
    public String toString() {
        return "AktørId{" + "value='******'}";
    }
}
