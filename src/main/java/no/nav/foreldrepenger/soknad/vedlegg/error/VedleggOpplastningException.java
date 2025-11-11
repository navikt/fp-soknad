package no.nav.foreldrepenger.soknad.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

public class VedleggOpplastningException extends RuntimeException {

    private final transient MediaType mediaType;

    public VedleggOpplastningException(String msg) {
        this(msg, null, null);
    }

    public VedleggOpplastningException(String msg, MediaType mediaType, Throwable e) {
        super(msg, e);
        this.mediaType = mediaType;
    }

    public String getFormatertMessage() {
        if (mediaType == null) {
            return getMessage();
        }
        return String.format("Melding: %s, mediaType=%s", getMessage(), mediaType);
    }
}
