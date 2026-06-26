package no.nav.foreldrepenger.soknad.innsending.fordel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.KonfigVerdi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

@ApplicationScoped
public class ProsessTaskGruppeUtleder {

    private static final String HMAC_ALGORITME = "HmacSHA256";

    private byte[] hmacKey;


    public ProsessTaskGruppeUtleder() {
        // CDI
    }

    @Inject
    public ProsessTaskGruppeUtleder(@KonfigVerdi(value = "PT_UID_HMAC_KEY") String hmacKeyBase64) {
        Objects.requireNonNull(hmacKeyBase64, "PT_UID_HMAC_KEY mangler");
        this.hmacKey = Base64.getDecoder().decode(hmacKeyBase64);
    }

    public String prosesstaskGruppeFor(String brukerIdent) {
        Objects.requireNonNull(brukerIdent, "brukerIdent mangler");
        return hmacHex(brukerIdent).substring(0, 16);
    }

    private String hmacHex(String brukerIdent) {
        try {
            var mac = Mac.getInstance(HMAC_ALGORITME);
            mac.init(new SecretKeySpec(hmacKey, HMAC_ALGORITME));
            var tagBytes = mac.doFinal(brukerIdent.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(tagBytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 ikke tilgjengelig", e); // er tilgjengelig i JVM
        }
    }
}
