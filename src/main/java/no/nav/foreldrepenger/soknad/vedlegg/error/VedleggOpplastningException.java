package no.nav.foreldrepenger.soknad.vedlegg.error;


import java.net.HttpURLConnection;

import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLLogLevel;

public class VedleggOpplastningException extends TekniskException {

    public VedleggOpplastningException(String msg) {
        this(msg, null, null);
    }

    public VedleggOpplastningException(String msg, MediaType mediaType) {
        this(msg, mediaType, null);
    }

    public VedleggOpplastningException(String msg, Throwable e) {
        this(msg, null, e);
    }

    public VedleggOpplastningException(String msg, MediaType mediaType, Throwable e) {
        super(null, getFormatertMessage("Vedlegg opplastning feilet: " + msg, mediaType), e);
    }

    public static String getFormatertMessage(String message, MediaType mediaType) {
        return mediaType == null ? message : String.format("%s, mediaType=%s", message, mediaType);
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_BAD_REQUEST;
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.MELLOMLAGRING_VEDLEGG.name();
    }

    @Override
    public VLLogLevel getLogLevel() {
        return VLLogLevel.INFO;
    }
}
