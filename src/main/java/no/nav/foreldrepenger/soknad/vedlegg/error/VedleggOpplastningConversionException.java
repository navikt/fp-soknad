package no.nav.foreldrepenger.soknad.vedlegg.error;


import jakarta.ws.rs.core.MediaType;

public class VedleggOpplastningConversionException extends VedleggOpplastningException {

    public VedleggOpplastningConversionException(MediaType mediaType, Throwable e) {
        super("Konvertering av vedlegg feilet", mediaType, e);
    }

}
