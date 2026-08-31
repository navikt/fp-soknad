package no.nav.foreldrepenger.soknad.kontrakt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.foreldrepenger.kontrakter.felles.typer.Orgnummer;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.NæringDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class SøkerDtoTest {

    private static final Fødselsnummer FNR = new Fødselsnummer("12345678901");
    private static final SøkerDto.Navn NAVN = new SøkerDto.Navn("Per", null, "Pål");

    @Test
    void forelagte_aktiviteter_skal_overleve_jackson_roundtrip() {
        var søker = new SøkerDto(FNR, NAVN,
            List.of(new SøkerDto.Arbeidsforhold("Nav", new Orgnummer("889640782"), 100.0, LocalDate.of(2024, 1, 11), null)),
            List.of(new SøkerDto.Frilansoppdrag("Kulturskolen", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 1)),
                new SøkerDto.Frilansoppdrag("Ola Nordmann", LocalDate.of(2025, 2, 1), null)),
            List.of(new SøkerDto.SelvstendigNæring("Fiskeri AS", new Orgnummer("974760673"), NæringDto.Virksomhetstype.FISKE)));

        var json = DefaultJsonMapper.toJson(søker);
        var deserialisert = DefaultJsonMapper.fromJson(json, SøkerDto.class);

        assertThat(deserialisert).isEqualTo(søker);
    }

    @Test
    void manglende_lister_skal_defaultes_til_tomme_lister() {
        var søker = new SøkerDto(FNR, NAVN, List.of(), null, null);

        assertThat(søker.frilansoppdrag()).isEmpty();
        assertThat(søker.selvstendigNæring()).isEmpty();
    }

    @Test
    void søknad_fra_klient_uten_forelagt_frilans_og_næring_skal_deserialiseres() {
        var json = """
            {
              "fnr": "12345678901",
              "navn": { "fornavn": "Per", "etternavn": "Pål" },
              "arbeidsforhold": []
            }
            """;

        var søker = DefaultJsonMapper.fromJson(json, SøkerDto.class);

        assertThat(søker.frilansoppdrag()).isEmpty();
        assertThat(søker.selvstendigNæring()).isEmpty();
    }

    @Test
    void frilansoppdrag_uten_organisasjonsnummer_skal_være_gyldig() {
        // Oppdragsgiver for frilans kan være en privatperson, og da har vi kun navnet
        var søker = new SøkerDto(FNR, NAVN, List.of(),
            List.of(new SøkerDto.Frilansoppdrag("Ola Nordmann", LocalDate.of(2025, 2, 1), null)), List.of());

        assertThat(hentValidator().validate(søker)).isEmpty();
    }

    @Test
    void frilansoppdrag_uten_fom_skal_gi_valideringsfeil() {
        // Uten fom klarer vi ikke å dokumentere perioden i kvitteringen, så søknaden må avvises ved innsending
        var søker = new SøkerDto(FNR, NAVN, List.of(), List.of(new SøkerDto.Frilansoppdrag("Kulturskolen", null, null)), List.of());

        var violations = hentValidator().validate(søker);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath()).hasToString("frilansoppdrag[0].fom");
    }

    @Test
    void selvstendig_næring_uten_næringstype_skal_være_gyldig() {
        // Næringstypen utledes av en BRREG-mapping der ukjente verdier er tenkelige, og skal ikke føre til avvist søknad
        var søker = new SøkerDto(FNR, NAVN, List.of(), List.of(),
            List.of(new SøkerDto.SelvstendigNæring("Sagene Fiskeri", new Orgnummer("974760673"), null)));

        assertThat(hentValidator().validate(søker)).isEmpty();
    }

    @Test
    void selvstendig_næring_med_ugyldig_organisasjonsnummer_skal_gi_valideringsfeil() {
        var søker = new SøkerDto(FNR, NAVN, List.of(), List.of(),
            List.of(new SøkerDto.SelvstendigNæring("Fiskeri AS", new Orgnummer("123456789"), NæringDto.Virksomhetstype.FISKE)));

        assertThat(hentValidator().validate(søker)).hasSize(1);
    }

    private static Validator hentValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
