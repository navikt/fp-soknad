package no.nav.foreldrepenger.soknad.server.error;

import no.nav.vedtak.log.mdc.MDCOperations;

public record ProblemDetails(FeilKode feilKode, int status, String message, String callId) {

    public ProblemDetails(FeilKode feilKode, int status, String message) {
        this(feilKode, status, message, MDCOperations.getCallId());
    }
}
