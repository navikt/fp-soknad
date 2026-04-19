package no.nav.foreldrepenger.soknad.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

public class VedleggOpplastningTypeUnsupportedException extends VedleggOpplastningException {

    public VedleggOpplastningTypeUnsupportedException(MediaType mediaType) {
        super("Mediatype ikke støttet", mediaType);
    }
}
