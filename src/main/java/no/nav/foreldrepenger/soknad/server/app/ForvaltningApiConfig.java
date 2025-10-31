package no.nav.foreldrepenger.soknad.server.app;

import static no.nav.foreldrepenger.soknad.server.app.ApiConfig.getApplicationProperties;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.soknad.server.AuthenticationFilter;
import no.nav.foreldrepenger.soknad.server.JacksonJsonConfig;
import no.nav.foreldrepenger.soknad.server.app.swagger.ForvaltningOpenApiResource;
import no.nav.foreldrepenger.soknad.server.app.swagger.FrontendOpenApiResource;
import no.nav.foreldrepenger.soknad.server.error.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.soknad.server.error.ValidationExceptionMapper;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends ResourceConfig {
    public static final String API_URI ="/forvaltning/api";

    public ForvaltningApiConfig() {
        register(AuthenticationFilter.class); // Sikkerhet
        register(GeneralRestExceptionMapper.class); // Exception handling
        register(ValidationExceptionMapper.class); // Exception handling
        register(JacksonJsonConfig.class); // Json
        register(ForvaltningOpenApiResource.class);
        register(FrontendOpenApiResource.class);
        registerClasses(getForvaltningKlasser());
        setProperties(getApplicationProperties());
    }

    public static Set<Class<?>> getForvaltningKlasser() {
        return Set.of(ProsessTaskRestTjeneste.class);
    }
}
