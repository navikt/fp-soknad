package no.nav.foreldrepenger.soknad.kontrakt.validering;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class GyldigLandkodeValidator implements ConstraintValidator<GyldigLandkode, String> {
    @Override
    public boolean isValid(String landkode, ConstraintValidatorContext context) {
        return landkode == null || Landkode.erGyldig(landkode);
    }
}
