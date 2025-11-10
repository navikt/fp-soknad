package no.nav.foreldrepenger.soknad.innsending.error;

public class DuplikatInnsendingException extends RuntimeException {
    public DuplikatInnsendingException(String msg) {
        super(msg);
    }
}
