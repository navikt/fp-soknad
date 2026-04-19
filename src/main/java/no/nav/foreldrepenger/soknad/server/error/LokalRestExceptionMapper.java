package no.nav.foreldrepenger.soknad.server.error;

import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import no.nav.vedtak.server.rest.FeilUtils;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@Provider
public class LokalRestExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(LokalRestExceptionMapper.class);
    private static final Logger SECURE_LOG = LoggerFactory.getLogger("secureLogger");

    @Override
    public Response toResponse(Throwable exception) {
        FeilUtils.ensureCallId();
        // 38 tilfelle siste 30 dager. Håndter som vanlig, evt med loggfilter
        if (exception instanceof EofException e) {
            LOG.info("Klient har avbrutt forespørsel (Early EOF).", e);
            return FeilUtils.responseFra(Response.Status.BAD_REQUEST.getStatusCode(), LokalFeilkode.MELLOMLAGRING_VEDLEGG.name(), e.getMessage());
        }
        FeilUtils.loggFeil(exception);
        // TODO: Er det verdt å beholde denne sikker-loggingen. Ellers kan generell metode brukes
        SECURE_LOG.info("Fikk uventet feil for bruker: {}", KontekstHolder.getKontekst().getUid(), exception);
        return FeilUtils.responseFra(exception);
    }
}
