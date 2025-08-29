package no.nav.foreldrepenger.soknad.server.error;


public record ProblemDetails(FeilKode feilKode, int status, String message) {

}
