package no.nav.foreldrepenger.soknad.server.app;


import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.soknad.server.HealtCheckRest;
import no.nav.foreldrepenger.soknad.server.PrometheusRestService;

@ApplicationPath(InternalApiConfig.API_URI)
public class InternalApiConfig extends ResourceConfig {

    public static final String API_URI ="/internal";

    public InternalApiConfig() {
        register(HealtCheckRest.class);
        register(PrometheusRestService.class);
    }
}
