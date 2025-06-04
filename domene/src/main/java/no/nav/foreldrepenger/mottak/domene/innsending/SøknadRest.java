package no.nav.foreldrepenger.mottak.domene.innsending;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;

@Path("/saker")
@ApplicationScoped
@Transactional
public class SøknadRest {
}
