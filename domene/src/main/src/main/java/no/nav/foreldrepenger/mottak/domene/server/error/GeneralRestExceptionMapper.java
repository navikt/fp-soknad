package no.nav.foreldrepenger.mottak.domene.server.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import no.nav.vedtak.exception.ManglerTilgangException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ManglerTilgangException manglerTilgangException) {
            return Response.status(403)
                .entity(problemDetails(manglerTilgangException))
                .build();
        }
        LOG.warn("Fikk uventet feil: {}", exception.getMessage(), exception);
        return Response.status(500).build();
    }

    static ProblemDetails problemDetails(ManglerTilgangException exception) {
        return new ProblemDetails(FeilKode.IKKE_TILGANG, 403, exception.getMessage());
    }
}
