package no.nav.foreldrepenger.soknad.mellomlagring;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;


@ApplicationScoped
public class MellomlagringKrypto {
    private static final char[] HEXCODE = "0123456789ABCDEF".toCharArray();

    private String passphrase;
    private InnloggetBruker innloggetBruker;

    public MellomlagringKrypto() {
        // CDI
    }

    @Inject
    public MellomlagringKrypto(@KonfigVerdi(value = "KRYPTERING_NOKKEL") String passphrase,
                               InnloggetBruker innloggetBruker) {
        this.passphrase = passphrase;
        this.innloggetBruker = innloggetBruker;
    }

    public String mappenavn() {
        return hexBinary(encrypt(innloggetBruker.brukerFraKontekst()).getBytes());
    }

    public String encrypt(String plaintext) {
        return new KrypteringHjelper(passphrase, innloggetBruker.brukerFraKontekst()).encrypt(plaintext);
    }

    public byte[] encryptVedlegg(byte[] innhold) {
        return new KrypteringHjelper(passphrase, innloggetBruker.brukerFraKontekst()).encryptVedlegg(innhold);
    }

    public String decrypt(String encrypted) {
        return new KrypteringHjelper(passphrase, innloggetBruker.brukerFraKontekst()).decrypt(encrypted);
    }

    public byte[] decryptVedlegg(byte[] encrypted) {
        return new KrypteringHjelper(passphrase, innloggetBruker.brukerFraKontekst()).decryptVedlegg(encrypted);
    }

    public String hexBinary(byte[] data) {
        var r = new StringBuilder(data.length * 2);
        for (var b : data) {
            r.append(HEXCODE[(b >> 4) & 0xF]);
            r.append(HEXCODE[(b & 0xF)]);
        }
        return r.toString();
    }


}
