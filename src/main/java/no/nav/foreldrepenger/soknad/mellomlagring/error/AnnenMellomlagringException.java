package no.nav.foreldrepenger.soknad.mellomlagring.error;

import java.net.HttpURLConnection;

import com.google.cloud.storage.StorageException;

import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.VLLogLevel;

public class AnnenMellomlagringException extends IntegrasjonException {

    private static final int TOOMANY = Response.Status.TOO_MANY_REQUESTS.getStatusCode();

    private final VLLogLevel level;

    public AnnenMellomlagringException(StorageException e) {
        super(null, e.getCode() == TOOMANY ? "For mange opplastnigner til samme objekt i gcp..." : "Mellomlagringsfeil (" + e.getCode() + ")",
            e.getCode() == TOOMANY ? TOOMANY : HttpURLConnection.HTTP_INTERNAL_ERROR,
            e.getCode() == TOOMANY ? null : e.getMessage(),
            e.getCode() == TOOMANY ? null : e.getCause());
        this.level = e.getCode() == TOOMANY ? VLLogLevel.INFO : VLLogLevel.WARN;
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.MELLOMLAGRING.name();
    }

    @Override
    public VLLogLevel getLogLevel() {
        return level;
    }
}
