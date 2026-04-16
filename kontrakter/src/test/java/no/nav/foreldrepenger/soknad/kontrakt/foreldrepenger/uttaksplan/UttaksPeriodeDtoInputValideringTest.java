package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType;
import no.nav.foreldrepenger.soknad.kontrakt.builder.UttakplanPeriodeBuilder;

class UttaksPeriodeDtoInputValideringTest {

    @Test
    void uttaksperiode_ønsker_samtidig_uttak_uten_prosent_skal_føre_til_valideringsfeil() {
        var uttaksperiode = UttakplanPeriodeBuilder
                .uttak(KontoType.FEDREKVOTE, LocalDate.now().minusWeeks(4), LocalDate.now())
                .medØnskerSamtidigUttak(true)
                .medSamtidigUttakProsent(null)
                .build();

        var validator = hentValidator();
        var resultat = validator.validate(uttaksperiode);
        assertThat(resultat).hasSize(1)
                .extracting(ConstraintViolation::getMessage)
                .contains("ønskerSamtidigUttak er satt, men ikke prosent, eller så er prosent satt og ikke ønskerSamtidigUttak");
    }


    @Test
    void uttaksperiode_ønsker_samtidig_uttak_med_prosent_skal_ikke_føre_til_valideringsfeil() {
        var uttaksperiode = UttakplanPeriodeBuilder
                .uttak(KontoType.FEDREKVOTE, LocalDate.now().minusWeeks(4), LocalDate.now())
                .medØnskerSamtidigUttak(true)
                .medSamtidigUttakProsent(20.2)
                .build();

        var validator = hentValidator();
        var resultat = validator.validate(uttaksperiode);
        assertThat(resultat).isEmpty();
    }

    @Test
    void uttaksperiode_ønsker_ikke_samtidig_uttak_men_med_prosent_skal_ikke_føre_til_valideringsfeil() {
        var uttaksperiode = UttakplanPeriodeBuilder
            .uttak(KontoType.FEDREKVOTE, LocalDate.now().minusWeeks(4), LocalDate.now())
            .medØnskerSamtidigUttak(false)
            .medSamtidigUttakProsent(20.2)
            .build();

        var validator = hentValidator();
        var resultat = validator.validate(uttaksperiode);
        assertThat(resultat).isEmpty();
    }

    @Test
    void uttaksperiode_ikke_samtidig_uttak_skal_ikke_føre_til_valideringsfeil() {
        var uttaksperiode = UttakplanPeriodeBuilder
                .uttak(KontoType.FEDREKVOTE, LocalDate.now().minusWeeks(4), LocalDate.now())
                .build();

        var validator = hentValidator();
        var resultat = validator.validate(uttaksperiode);
        assertThat(resultat).isEmpty();

    }

    private static Validator hentValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
