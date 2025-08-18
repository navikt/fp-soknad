package no.nav.foreldrepenger.mottak.domene.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

public class AttachmentConversionException extends AttachmentException {

    public AttachmentConversionException(String msg, Throwable e) {
        this(msg, null, e);
    }

    public AttachmentConversionException(String msg, MediaType mediaType, Throwable e) {
        super(msg, mediaType, e);
    }

}
