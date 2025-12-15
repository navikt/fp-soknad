package no.nav.foreldrepenger.soknad.innsending;

public class DuplikateVedleggException extends RuntimeException {
    public DuplikateVedleggException(String msg) {
        super(msg);
    }
}
