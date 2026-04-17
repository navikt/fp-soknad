package no.nav.foreldrepenger.soknad.mellomlagring.error;

import java.net.HttpURLConnection;

import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.VLLogLevel;

public class KrypteringMellomlagringException extends IntegrasjonException {

    public KrypteringMellomlagringException(String msg, Throwable cause) {
        super(null, "Krypteringsfeil ved mellomlagring: " + msg,
            HttpURLConnection.HTTP_BAD_REQUEST,
            cause);
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.KRYPTERING_MELLOMLAGRING.name();
    }

    @Override
    public VLLogLevel getLogLevel() {
        return VLLogLevel.INFO;
    }
}
