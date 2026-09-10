package no.nav.foreldrepenger.soknad.kontrakt;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.soknad.kontrakt.validering.GyldigLandkode;

public record UtenlandsoppholdsperiodeDto(@NotNull LocalDate fom, LocalDate tom, @NotNull @GyldigLandkode String landkode) {

    @AssertTrue(message = "FOM dato må være etter TOM dato!")
    public boolean isFomFørTom() { //NOSONAR. Må starte med is/has/get for at AssertTrue skal fungere
        if (tom() == null) {
            return true;
        }
        return fom().isBefore(tom());
    }
}
