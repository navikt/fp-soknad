package no.nav.foreldrepenger.soknad.vedlegg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VedleggUtilTest {


    @Test
    void bytes_til_megabytes_test() {
        var bytes = 1048576L; // 1 MB in bytes
        var megabytes = VedleggUtil.megabytes(bytes);
        System.out.println("Megabytes: " + megabytes);
    }
}
