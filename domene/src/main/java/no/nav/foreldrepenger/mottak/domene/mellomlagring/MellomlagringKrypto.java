package no.nav.foreldrepenger.mottak.domene.mellomlagring;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;


@ApplicationScoped
public class MellomlagringKrypto {
    private static final char[] HEXCODE = "0123456789ABCDEF".toCharArray();

    private String passphrase;

    public MellomlagringKrypto() {
        // CDI
    }

    @Inject
    public MellomlagringKrypto(@KonfigVerdi(value = "KRYPTERING_NOKKEL") String passphrase) {
        this.passphrase = passphrase;
    }

    public String mappenavn() {
        return hexBinary(encrypt(brukerFraKonteks()).getBytes());
    }

    public String encrypt(String plaintext) {
        return new KrypteringHjelper(passphrase, brukerFraKonteks()).encrypt(plaintext);
    }

    public byte[] encryptVedlegg(byte[] innhold) {
        return new KrypteringHjelper(passphrase, brukerFraKonteks()).encryptVedlegg(innhold);
    }

    public String decrypt(String encrypted) {
        return new KrypteringHjelper(passphrase, brukerFraKonteks()).decrypt(encrypted);
    }

    public byte[] decryptVedlegg(byte[] encrypted) {
        return new KrypteringHjelper(passphrase, brukerFraKonteks()).decryptVedlegg(encrypted);
    }

    public String hexBinary(byte[] data) {
        var r = new StringBuilder(data.length * 2);
        for (var b : data) {
            r.append(HEXCODE[(b >> 4) & 0xF]);
            r.append(HEXCODE[(b & 0xF)]);
        }
        return r.toString();
    }

    public String brukerFraKonteks() {
        return KontekstHolder.getKontekst().getUid();
    }
}
