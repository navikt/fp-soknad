package no.nav.foreldrepenger.soknad.vedlegg.error;


import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;

public class VedleggOpplastningPasswordProtectedException extends VedleggOpplastningException {

    public VedleggOpplastningPasswordProtectedException(MediaType mediaType) {
        super("Dokumentet kan ikke håndteres av NAV ettersom det er passordbeskyttet.", mediaType);
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.MELLOMLAGRING_VEDLEGG_PASSORD_BESKYTTET.name();
    }

}
