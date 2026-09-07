package no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.soknad.kontrakt.UtenlandsoppholdsperiodeDto;

class V3DomainMapperCommonTest {

    @Test
    void normaliserer_alpha3_i_xml_modellen() {
        assertThat(V3DomainMapperCommon.landFraLandkode("NOR").getKode()).isEqualTo("NOR");
        assertThat(V3DomainMapperCommon.landFraLandkode("nor").getKode()).isEqualTo("NOR");
    }

    @Test
    void viderefører_kosovo_håndtering() {
        assertThat(V3DomainMapperCommon.landFraLandkode("XXK").getKode()).isEqualTo("XXK");
    }

    @Test
    void gjenkjenner_norge_fra_alpha3() {
        var dato = LocalDate.now();
        var opphold = List.of(new UtenlandsoppholdsperiodeDto(dato.minusDays(1), dato.plusDays(1), "NOR"));

        assertThat(V3DomainMapperCommon.varINorge(opphold, dato)).isTrue();
    }
}
