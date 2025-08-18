package no.nav.foreldrepenger.mottak.domene.vedlegg.error;



public class AttachmentPasswordProtectedException extends AttachmentException {

    public AttachmentPasswordProtectedException() {
        super("Dokumentet kan ikke håndteres av NAV ettersom det er passordbeskyttet.");
    }

}
