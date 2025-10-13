package no.nav.foreldrepenger.soknad.vedlegg.error;

import jakarta.ws.rs.core.MediaType;

public class VedleggOpplastningUnreadableException extends VedleggOpplastningException {

    public VedleggOpplastningUnreadableException(String msg, MediaType mediaType) {
        super(msg, mediaType, null);
    }

    public VedleggOpplastningUnreadableException(String msg, MediaType mediaType, Throwable throwable) {
        super(msg, mediaType, throwable);
    }
}
