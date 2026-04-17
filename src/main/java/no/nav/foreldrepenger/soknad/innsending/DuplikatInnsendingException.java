package no.nav.foreldrepenger.soknad.innsending;

import java.net.HttpURLConnection;

import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.VLLogLevel;

public class DuplikatInnsendingException extends FunksjonellException {
    public DuplikatInnsendingException(String msg) {
        super(null, msg);
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_CONFLICT;
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.DUPLIKAT_FORSENDELSE.name();
    }

    @Override
    public VLLogLevel getLogLevel() {
        return VLLogLevel.INFO;
    }
}
