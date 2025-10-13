package no.nav.foreldrepenger.soknad.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

public class VedleggOpplastningPasswordProtectedException extends VedleggOpplastningException {

    public VedleggOpplastningPasswordProtectedException(MediaType mediaType) {
        super("Dokumentet kan ikke håndteres av NAV ettersom det er passordbeskyttet.", mediaType, null);
    }

}
