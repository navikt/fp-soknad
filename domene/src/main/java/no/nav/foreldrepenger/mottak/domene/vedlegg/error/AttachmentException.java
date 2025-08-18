package no.nav.foreldrepenger.mottak.domene.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

// TODO: Userfacing error messages should be localized and fetched from a message source
public abstract class AttachmentException extends RuntimeException {

    private final MediaType mediaType;

    protected AttachmentException(String msg) {
        this(msg, null);
    }

    protected AttachmentException(String msg, Exception cause) {
        this(msg, null, cause);
    }

    protected AttachmentException(String msg, MediaType mediaType, Throwable e) {
        super(msg, e);
        this.mediaType = mediaType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}
