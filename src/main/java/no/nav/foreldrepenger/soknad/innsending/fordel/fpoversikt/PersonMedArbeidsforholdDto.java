package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.foreldrepenger.common.domain.Navn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;


public record PersonMedArbeidsforholdDto(PersonDto person, List<ArbeidsforholdDto> arbeidsforhold) {
    record PersonDto(@Valid @NotNull AktørId aktørid, @Valid @NotNull Fødselsnummer fnr, @Valid @NotNull Navn navn) {
    }

    record ArbeidsforholdDto(String navn, @Valid Stillingsprosent stillingsprosent, LocalDate from, LocalDate to) {
    }

    record Stillingsprosent(@JsonValue BigDecimal prosent) {

    }
}
