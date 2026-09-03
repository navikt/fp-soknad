package no.nav.foreldrepenger.soknad.kontrakt.validering;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import no.nav.foreldrepenger.soknad.kontrakt.UtenlandsoppholdsperiodeDto;

class LandkodeTest {

    @Test
    void normaliserer_alpha2_til_alpha3() {
        assertThat(Landkode.normaliser("no")).isEqualTo("NOR");
    }

    @Test
    void beholder_alpha3() {
        assertThat(Landkode.normaliser("nor")).isEqualTo("NOR");
    }

    @Test
    void viderefører_kosovo_håndtering() {
        assertThat(Landkode.normaliser("XK")).isEqualTo("XXK");
        assertThat(Landkode.normaliser("XXK")).isEqualTo("XXK");
    }

    @Test
    void avviser_ukjent_landkode() {
        assertThat(Landkode.erGyldig("ZZZ")).isFalse();
    }

    @Test
    void godtar_rå_alpha2_og_alpha3() {
        assertThat(Landkode.erGyldig("NO")).isTrue();
        assertThat(Landkode.erGyldig("NOR")).isTrue();
        assertThat(Landkode.erGyldig("XK")).isTrue();
        assertThat(Landkode.erGyldig("XXK")).isTrue();
    }

    @Test
    void ugyldig_landkode_gir_valideringsfeil() {
        var utenlandsopphold = new UtenlandsoppholdsperiodeDto(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), "ZZZ");

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(utenlandsopphold))
                .extracting(violation -> violation.getMessage())
                .containsExactly("Landkode er ugyldig");
        }
    }
}
