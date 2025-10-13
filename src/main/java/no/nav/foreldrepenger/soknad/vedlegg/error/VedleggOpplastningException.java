package no.nav.foreldrepenger.soknad.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

public abstract class VedleggOpplastningException extends RuntimeException {

    private final MediaType mediaType;

    protected VedleggOpplastningException(String msg) {
        this(msg, null, null);
    }

    protected VedleggOpplastningException(String msg, MediaType mediaType, Throwable e) {
        super(msg, e);
        this.mediaType = mediaType;
    }

    public String getFormatertMessage() {
        if (mediaType == null) {
            return String.format("Opplastning av vedlegg feilet: %s", getMessage());
        }
        return String.format("Opplastning av vedlegg feilet: %s, mediaType=%s", getMessage(), mediaType);
    }
}
