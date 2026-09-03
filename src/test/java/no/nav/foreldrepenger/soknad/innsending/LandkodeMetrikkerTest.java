package no.nav.foreldrepenger.soknad.innsending;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SvangerskapspengesøknadDto;
import org.junit.jupiter.api.Test;

class LandkodeMetrikkerTest {

    @Test
    void identifiserer_alpha2() {
        assertThat(LandkodeMetrikker.erAlpha2("NO")).isTrue();
        assertThat(LandkodeMetrikker.erAlpha2("XK")).isTrue();
        assertThat(LandkodeMetrikker.erAlpha2("NOR")).isFalse();
        assertThat(LandkodeMetrikker.erAlpha2("XXK")).isFalse();
    }

    @Test
    void grupperer_søknader_i_tre_søknadstyper() {
        assertThat(LandkodeMetrikker.søknadstype(mock(ForeldrepengesøknadDto.class))).isEqualTo("FP");
        assertThat(LandkodeMetrikker.søknadstype(mock(EndringssøknadForeldrepengerDto.class))).isEqualTo("FP");
        assertThat(LandkodeMetrikker.søknadstype(mock(SvangerskapspengesøknadDto.class))).isEqualTo("SVP");
        assertThat(LandkodeMetrikker.søknadstype(mock(EngangsstønadDto.class))).isEqualTo("ES");
    }

    @Test
    void håndterer_utelatte_lister() {
        assertThatCode(() -> LandkodeMetrikker.registrer(mock(ForeldrepengesøknadDto.class))).doesNotThrowAnyException();
        assertThatCode(() -> LandkodeMetrikker.registrer(mock(SvangerskapspengesøknadDto.class))).doesNotThrowAnyException();
        assertThatCode(() -> LandkodeMetrikker.registrer(mock(EngangsstønadDto.class))).doesNotThrowAnyException();
    }

    @Test
    void lar_ikke_feil_i_metrikkregistrering_stoppe_innsending() {
        var søknad = mock(ForeldrepengesøknadDto.class);
        when(søknad.utenlandsopphold()).thenThrow(new RuntimeException("metrikkfeil"));

        assertThatCode(() -> LandkodeMetrikker.registrer(søknad)).doesNotThrowAnyException();
    }
}
