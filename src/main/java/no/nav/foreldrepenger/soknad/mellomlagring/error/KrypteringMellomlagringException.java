package no.nav.foreldrepenger.soknad.mellomlagring.error;

public class KrypteringMellomlagringException extends RuntimeException {

    public KrypteringMellomlagringException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
