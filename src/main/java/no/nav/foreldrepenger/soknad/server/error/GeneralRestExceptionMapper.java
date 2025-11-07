package no.nav.foreldrepenger.soknad.server.error;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.storage.StorageException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import no.nav.foreldrepenger.soknad.innsending.DuplikatInnsendingException;
import no.nav.foreldrepenger.soknad.mellomlagring.error.KrypteringMellomlagringException;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningException;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggVirusscanTimeoutException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);
    private static final Logger SECURE_LOG = LoggerFactory.getLogger("secureLogger");

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ManglerTilgangException e) {
            return status(HttpStatus.FORBIDDEN_403, FeilKode.IKKE_TILGANG, e.getMessage());
        }
        if (exception instanceof VedleggVirusscanTimeoutException e) {
            LOG.info("Virusscan tok lenger tid enn satt timeout. Clavmav cacher og scan vil gå fortere neste gang.", e);
            return status(HttpStatus.SERVICE_UNAVAILABLE_503, FeilKode.MELLOMLAGRING_VEDLEGG_OPPLASTNING, e.getMessage());
        }
        if (exception instanceof VedleggOpplastningException e) {
            LOG.info("Vedlegg opplastning feilet: {}", e.getFormatertMessage());
            return status(HttpStatus.BAD_REQUEST_400, FeilKode.MELLOMLAGRING_VEDLEGG_OPPLASTNING, e.getFormatertMessage());
        }
        if (exception instanceof StorageException e) {
            LOG.warn("Mellomlagring feilet ({})", e.getCode(), e);
            return status(HttpStatus.INTERNAL_SERVER_ERROR_500, FeilKode.MELLOMLAGRING, e.getMessage());
        }
        if (exception instanceof KrypteringMellomlagringException e) {
            LOG.error("Feil ved kryptering av mellomlagret data: {}", e.getMessage(), e);
            return status(HttpStatus.BAD_REQUEST_400, FeilKode.IKKE_TILGANG, e.getMessage());
        }
        if (exception instanceof DuplikatInnsendingException e) {
            LOG.info(e.getMessage());
            return status(HttpStatus.CONFLICT_409, FeilKode.DUPLIKAT_FORSENDELSE, e.getMessage());
        }
        if (exception instanceof EofException e) {
            LOG.info("Klient har avbrutt forespørsel (Early EOF).", e);
            return status(HttpStatus.BAD_REQUEST_400, FeilKode.MELLOMLAGRING_VEDLEGG_OPPLASTNING, e.getMessage());
        }
        LOG.warn("Fikk uventet feil: {}", exception.getMessage(), exception);
        SECURE_LOG.info("Fikk uventet feil for bruker: {}", KontekstHolder.getKontekst().getUid(), exception);
        return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500).build();
    }

    private static Response status(int httpstatus, FeilKode feilKode, String formatertMessage) {
        return Response.status(httpstatus).entity(new ProblemDetails(feilKode, httpstatus, formatertMessage)).build();
    }
}
