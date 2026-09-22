package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class FpoversiktRestKlientTest {

    @Test
    void skalKunneOpprettesMedKonfigurertEndpoint() {
        assertThatCode(FpoversiktRestKlient::new).doesNotThrowAnyException();
    }
}
