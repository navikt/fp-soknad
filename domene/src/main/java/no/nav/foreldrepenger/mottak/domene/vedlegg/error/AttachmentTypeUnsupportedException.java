package no.nav.foreldrepenger.mottak.domene.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

public class AttachmentTypeUnsupportedException extends AttachmentException {

    public AttachmentTypeUnsupportedException(MediaType mediaType) {
        this("Media type " + mediaType + " er ikke støttet", mediaType, null);
    }

    public AttachmentTypeUnsupportedException(String msg, MediaType mediaType, Throwable e) {
        super(msg, mediaType, e);
    }

}
