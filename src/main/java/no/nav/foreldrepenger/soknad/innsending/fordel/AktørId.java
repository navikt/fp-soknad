package no.nav.foreldrepenger.soknad.innsending.fordel;

import static no.nav.foreldrepenger.kontrakter.fpsoknad.validering.InputValideringRegex.BARE_TALL;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.Pattern;

public record AktørId(@JsonValue @Pattern(regexp = BARE_TALL) String value)  {

    @Override
    public String toString() {
        return "AktørId{" + "value='*****'}";
    }
}
