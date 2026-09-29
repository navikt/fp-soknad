package no.nav.foreldrepenger.soknad.kontrakt;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FELLESPERIODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UtsettelsesPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UtsettelsesÅrsak;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class FellesUttaksplanKontraktTest {

    private static final String PERIODER_JSON = """
        [{
          "fom": "2026-01-05",
          "tom": "2026-01-09",
          "søker": {
            "forelder": "MOR",
            "kontoType": "FELLESPERIODE",
            "flerbarnsdager": false,
            "resultat": {
              "innvilget": true,
              "trekkerMinsterett": false,
              "trekkerDager": true,
              "årsak": "ANNET"
            }
          },
          "annenPart": {
            "forelder": "FAR_MEDMOR",
            "kontoType": "FEDREKVOTE",
            "flerbarnsdager": false
          },
          "annenPartEøs": {
            "kontoType": "FELLESPERIODE",
            "trekkdager": 5
          }
        }]
        """;

    private static final String LEGACY_PERIODER_JSON = """
        [{
          "type": "uttak",
          "fom": "2026-01-05",
          "tom": "2026-01-09",
          "konto": "FELLESPERIODE"
        }]
        """;

    @Test
    void felles_uttaksplan_beholder_jsonformat_og_deler_periodetype_med_søknaden() throws Exception {
        var json = """
            {"termindato": "2026-01-05", "antallBarn": 1, "dekningsgrad": "HUNDRE", "perioder": %s}
            """.formatted(PERIODER_JSON);
        var plan = DefaultJsonMapper.fromJson(json, FellesUttaksplanDto.class);
        var søknad = DefaultJsonMapper.fromJson("""
            {"uttaksplan": {"uttaksperioder": [], "perioder": %s}}
            """.formatted(PERIODER_JSON), ForeldrepengesøknadDto.class);

        assertThat(søknad.uttaksplan().perioder()).isEqualTo(plan.perioder());
        var jsonMapper = DefaultJsonMapper.getJsonMapper();
        assertThat(jsonMapper.readTree(DefaultJsonMapper.toJson(plan))).isEqualTo(jsonMapper.readTree(json));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(plan)).isEmpty();
        }
    }

    @Test
    void foreldrepengesøknad_normaliserer_legacy_og_beholder_nye_perioder() {
        var søknad = DefaultJsonMapper.fromJson("""
            {
              "dekningsgrad": "100",
              "uttaksplan": {
                "ønskerJustertUttakVedFødsel": true,
                "uttaksperioder": [],
                "perioder": %s
              }
            }
            """.formatted(PERIODER_JSON), ForeldrepengesøknadDto.class);

        assertThat(søknad.uttaksplan().ønskerJustertUttakVedFødsel()).isTrue();
        assertPlanOgSerialisering(søknad.uttaksplan(), DefaultJsonMapper.toJson(søknad));
        var gjenlest = DefaultJsonMapper.fromJson(DefaultJsonMapper.toJson(søknad), ForeldrepengesøknadDto.class);
        assertThat(gjenlest.uttaksplan()).isEqualTo(søknad.uttaksplan());
    }

    @Test
    void endringssøknad_normaliserer_legacy_og_beholder_nye_perioder() {
        var søknad = DefaultJsonMapper.fromJson("""
            {
              "uttaksplan": {
                "ønskerJustertUttakVedFødsel": false,
                "uttaksperioder": %s,
                "perioder": %s
              }
            }
            """.formatted(LEGACY_PERIODER_JSON, PERIODER_JSON), EndringssøknadForeldrepengerDto.class);

        assertThat(søknad.uttaksplan().ønskerJustertUttakVedFødsel()).isFalse();
        assertPlanOgSerialisering(søknad.uttaksplan(), DefaultJsonMapper.toJson(søknad));
        var gjenlest = DefaultJsonMapper.fromJson(DefaultJsonMapper.toJson(søknad), EndringssøknadForeldrepengerDto.class);
        assertThat(gjenlest.uttaksplan()).isEqualTo(søknad.uttaksplan());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ", \"perioder\": null"})
    void gammel_json_og_null_beholder_legacy_plan(String perioder) {
        var json = """
            {"uttaksplan": {"ønskerJustertUttakVedFødsel": true, "uttaksperioder": %s%s}}
            """.formatted(LEGACY_PERIODER_JSON, perioder);

        var førstegang = DefaultJsonMapper.fromJson(json, ForeldrepengesøknadDto.class);
        var endring = DefaultJsonMapper.fromJson(json, EndringssøknadForeldrepengerDto.class);

        assertThat(førstegang.uttaksplan()).isEqualTo(endring.uttaksplan());
        assertThat(førstegang.uttaksplan().perioder()).isNull();
        assertThat(førstegang.uttaksplan().ønskerJustertUttakVedFødsel()).isTrue();
        assertThat(førstegang.uttaksplan().uttaksperioder()).singleElement().isInstanceOf(UttaksPeriodeDto.class);
        assertThat(new UttaksplanDto(true, førstegang.uttaksplan().uttaksperioder())).isEqualTo(førstegang.uttaksplan());
    }

    @Test
    void tom_ny_plan_tømmer_førstegang_og_overlever_serialisering() {
        var søknad = DefaultJsonMapper.fromJson("""
            {"uttaksplan": {"uttaksperioder": %s, "perioder": []}}
            """.formatted(LEGACY_PERIODER_JSON), ForeldrepengesøknadDto.class);

        assertThat(søknad.uttaksplan().uttaksperioder()).isEmpty();
        assertThat(søknad.uttaksplan().perioder()).isEmpty();
        var gjenlest = DefaultJsonMapper.fromJson(DefaultJsonMapper.toJson(søknad), ForeldrepengesøknadDto.class);
        assertThat(gjenlest.uttaksplan()).isEqualTo(søknad.uttaksplan());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[]"})
    void endringssøknad_med_nye_perioder_avviser_manglende_eller_tom_legacy_plan(String legacyPerioder) {
        assertThatThrownBy(() -> DefaultJsonMapper.fromJson("""
            {"uttaksplan": {"uttaksperioder": %s, "perioder": %s}}
            """.formatted(legacyPerioder, PERIODER_JSON), EndringssøknadForeldrepengerDto.class))
            .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", PERIODER_JSON})
    void endring_beholder_fri_markør_og_avgrenser_full_plan_ved_serialisering(String perioder) {
        var søknad = DefaultJsonMapper.fromJson("""
            {
              "uttaksplan": {
                "ønskerJustertUttakVedFødsel": true,
                "uttaksperioder": [{
                  "type": "utsettelse",
                  "fom": "2026-01-12",
                  "tom": "2026-01-16",
                  "årsak": "FRI"
                }],
                "perioder": %s
              }
            }
            """.formatted(perioder), EndringssøknadForeldrepengerDto.class);

        assertThat(søknad.uttaksplan().uttaksperioder()).containsExactly(
            new UtsettelsesPeriodeDto(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 16), UtsettelsesÅrsak.FRI, null, false));
        assertThat(søknad.uttaksplan().ønskerJustertUttakVedFødsel()).isTrue();
        var gjenlest = DefaultJsonMapper.fromJson(DefaultJsonMapper.toJson(søknad), EndringssøknadForeldrepengerDto.class);
        assertThat(gjenlest.uttaksplan()).isEqualTo(søknad.uttaksplan());
    }

    @Test
    void validering_kaskaderer_fra_søknad_til_nye_perioder_og_avviser_null_elementer() {
        var ugyldigePerioder = """
            [null, {"tom": "2026-01-09", "søker": {"kontoType": "FELLESPERIODE",
              "gradering": {"arbeidstidprosent": 40, "aktivitet": {"arbeidsgiver": {}}}},
              "annenPart": {"resultat": {}},
              "annenPartEøs": {"kontoType": "FELLESPERIODE", "trekkdager": -1}}]
            """;
        var json = """
            {"uttaksplan": {"uttaksperioder": %s, "perioder": %s}}
            """.formatted(LEGACY_PERIODER_JSON, ugyldigePerioder);
        var søknader = List.of(
            DefaultJsonMapper.fromJson(json, ForeldrepengesøknadDto.class),
            DefaultJsonMapper.fromJson(json, EndringssøknadForeldrepengerDto.class));

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            for (var søknad : søknader) {
                assertThat(factory.getValidator().validate(søknad))
                    .extracting(feil -> feil.getPropertyPath().toString())
                    .contains(
                        "uttaksplan.perioder[0].<list element>",
                        "uttaksplan.perioder[1].fom",
                        "uttaksplan.perioder[1].søker.forelder",
                        "uttaksplan.perioder[1].søker.gradering.aktivitet.type",
                        "uttaksplan.perioder[1].søker.gradering.aktivitet.arbeidsgiver.id",
                        "uttaksplan.perioder[1].annenPart.forelder",
                        "uttaksplan.perioder[1].annenPart.resultat.årsak",
                        "uttaksplan.perioder[1].annenPartEøs.trekkdager.verdi");
            }
        }
    }

    @Test
    void ny_liste_er_valgfri_mens_legacy_liste_fortsatt_er_påkrevd() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(new UttaksplanDto(null, List.of()))).isEmpty();
            assertThat(validator.validate(new UttaksplanDto(null, List.of(), List.of()))).isEmpty();
            assertThat(validator.validate(new UttaksplanDto(null, null, List.of())))
                .extracting(feil -> feil.getPropertyPath().toString()).containsExactly("uttaksperioder");
        }
    }

    private static void assertPlanOgSerialisering(UttaksplanDto uttaksplan, String serialisertSøknad) {
        assertThat(uttaksplan.uttaksperioder()).singleElement().isInstanceOf(UttaksPeriodeDto.class);
        assertThat(((UttaksPeriodeDto) uttaksplan.uttaksperioder().getFirst()).konto()).isEqualTo(FELLESPERIODE);
        assertThat(uttaksplan.perioder()).singleElement().satisfies(periode -> {
            assertThat(periode.søker().resultat()).isNotNull();
            assertThat(periode.annenPart()).isNotNull();
            assertThat(periode.annenPartEøs()).isNotNull();
        });
        assertThat(serialisertSøknad).contains("\"uttaksplan\"", "\"uttaksperioder\"", "\"perioder\"")
            .doesNotContain("\"fellesUttaksplan\"", "\"termindato\"", "\"antallBarn\"");
    }
}
