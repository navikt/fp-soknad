package no.nav.foreldrepenger.soknad.vedlegg.error;



public class AttachmentPasswordProtectedException extends AttachmentException {

    public AttachmentPasswordProtectedException() {
        super("Dokumentet kan ikke håndteres av NAV ettersom det er passordbeskyttet.");
    }

}
