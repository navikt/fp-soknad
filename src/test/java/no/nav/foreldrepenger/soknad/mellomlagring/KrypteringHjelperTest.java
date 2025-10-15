package no.nav.foreldrepenger.soknad.mellomlagring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.soknad.mellomlagring.error.KrypteringMellomlagringException;

class KrypteringHjelperTest {

    @Test
    void kryptering_dekryptering_roundtrip_test() {
        var krypteringHjelper = new KrypteringHjelper("test-passphrase", "123456789");
        var original = "Dette er en test!";

        var kryptertStreng = krypteringHjelper.encrypt(original);
        var dekryptertStreng = krypteringHjelper.decrypt(kryptertStreng);

        assertThat(original).isEqualTo(dekryptertStreng);
    }

    @Test
    void kryptert_streng_skal_ikke_kunne_leses_av_andre_brukere() {
        var krypteringPerson1 = new KrypteringHjelper("test-passphrase", "123456789");
        var krypteringPerson2 = new KrypteringHjelper("test-passphrase", "0987654321");
        var original = "Dette er en test!";

        var kryptertStreng = krypteringPerson1.encrypt(original);
        assertThrows(KrypteringMellomlagringException.class, () -> krypteringPerson2.decrypt(kryptertStreng));
    }
}
