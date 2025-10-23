package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import static no.nav.vedtak.util.InputValideringRegex.FRITEKST;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.validering.Orgnr;

public record Orgnummer(@JsonValue @Pattern(regexp = FRITEKST) @NotNull @Orgnr String value){

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Orgnummer { // NOSONAR
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [orgnr='******']";
    }
}
