package no.nav.foreldrepenger.soknad.mellomlagring;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;


@ApplicationScoped
public class MellomlagringKrypto {

    private String passphrase;
    private InnloggetBruker innloggetBruker;

    public MellomlagringKrypto() {
        // CDI
    }

    @Inject
    public MellomlagringKrypto(@KonfigVerdi(value = "KRYPTERING_PASSWORD") String passphrase, InnloggetBruker innloggetBruker) {
        this.passphrase = passphrase;
        this.innloggetBruker = innloggetBruker;
    }

    public String mappenavn(YtelseMellomlagringType ytelse, boolean vedlegg) {
        var fnr = innloggetBruker.brukerFraKontekst();
        return new KrypteringHjelper(passphrase, fnr).uniktMappenavn(fnr, ytelse, vedlegg);
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
}
