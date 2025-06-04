package no.nav.foreldrepenger.mottak.domene.server.error;


public record ProblemDetails(FeilKode feilKode, int status, String message) {

}
