package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

class UttaksplanDtoInputValideringTest {

    @Test
    void null_liste_avvises() {
        var uttaksplan = new UttaksplanDto(null, null);

        assertThat(valider(uttaksplan))
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactly("perioder");
    }

    @Test
    void null_element_avvises() {
        var uttaksplan = new UttaksplanDto(null, Collections.singletonList(null));

        assertThat(valider(uttaksplan)).hasSize(1);
    }

    @Test
    void manglende_periode_datoer_avvises_kaskadert() {
        var periode = new UttakPeriodeDto(null, null, null, null, null);
        var uttaksplan = new UttaksplanDto(null, List.of(periode));

        assertThat(valider(uttaksplan))
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("perioder[0].fom", "perioder[0].tom");
    }

    @Test
    void gyldig_liste_godtas() {
        var periode = new UttakPeriodeDto(LocalDate.now().minusWeeks(4), LocalDate.now(), null, null, null);
        var uttaksplan = new UttaksplanDto(null, List.of(periode));

        assertThat(valider(uttaksplan)).isEmpty();
    }

    private static Set<ConstraintViolation<UttaksplanDto>> valider(UttaksplanDto uttaksplan) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(uttaksplan);
        }
    }
}
