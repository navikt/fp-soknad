package no.nav.foreldrepenger.soknad.kontrakt;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FELLESPERIODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksPeriodeDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class FellesUttaksplanKontraktTest {

    private static final String FELLES_UTTAKSPLAN_JSON = """
        {
          "termindato": "2026-01-01",
          "antallBarn": 1,
          "dekningsgrad": "HUNDRE",
          "perioder": [{
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
        }
        """;

    @Test
    void foreldrepengesøknad_aksepterer_felles_plan_og_serialiserer_legacy_plan() {
        var søknad = DefaultJsonMapper.fromJson("""
            {
              "dekningsgrad": "100",
              "uttaksplan": {
                "ønskerJustertUttakVedFødsel": true,
                "uttaksperioder": []
              },
              "fellesUttaksplan": %s
            }
            """.formatted(FELLES_UTTAKSPLAN_JSON), ForeldrepengesøknadDto.class);

        assertThat(søknad.uttaksplan().ønskerJustertUttakVedFødsel()).isTrue();
        assertPlanOgSerialisering(søknad.uttaksplan(), DefaultJsonMapper.toJson(søknad));
    }

    @Test
    void endringssøknad_aksepterer_felles_plan_og_serialiserer_legacy_plan() {
        var søknad = DefaultJsonMapper.fromJson("""
            {
              "uttaksplan": {
                "uttaksperioder": [{
                  "type": "uttak",
                  "fom": "2026-01-05",
                  "tom": "2026-01-09",
                  "konto": "FELLESPERIODE"
                }]
              },
              "fellesUttaksplan": %s
            }
            """.formatted(FELLES_UTTAKSPLAN_JSON), EndringssøknadForeldrepengerDto.class);

        assertPlanOgSerialisering(søknad.uttaksplan(), DefaultJsonMapper.toJson(søknad));
    }

    @Test
    void endringssøknad_med_felles_plan_avviser_manglende_eller_tom_legacy_plan() {
        assertThatThrownBy(() -> DefaultJsonMapper.fromJson("""
            {
              "fellesUttaksplan": %s
            }
            """.formatted(FELLES_UTTAKSPLAN_JSON), EndringssøknadForeldrepengerDto.class))
            .hasRootCauseInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> DefaultJsonMapper.fromJson("""
            {
              "uttaksplan": {
                "uttaksperioder": []
              },
              "fellesUttaksplan": %s
            }
            """.formatted(FELLES_UTTAKSPLAN_JSON), EndringssøknadForeldrepengerDto.class))
            .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    private static void assertPlanOgSerialisering(
        no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto uttaksplan,
        String serialisertSøknad) {
        assertThat(uttaksplan.uttaksperioder()).singleElement().isInstanceOf(UttaksPeriodeDto.class);
        assertThat(((UttaksPeriodeDto) uttaksplan.uttaksperioder().getFirst()).konto()).isEqualTo(FELLESPERIODE);
        assertThat(serialisertSøknad).contains("\"uttaksplan\"", "\"fellesUttaksplan\"");
    }
}
