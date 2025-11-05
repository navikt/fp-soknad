package no.nav.foreldrepenger.soknad.server.konfig;

import static no.nav.foreldrepenger.soknad.server.konfig.ApiConfig.getApplicationProperties;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.soknad.server.JacksonJsonConfig;
import no.nav.foreldrepenger.soknad.server.error.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.soknad.server.error.ValidationExceptionMapper;
import no.nav.foreldrepenger.soknad.server.forvaltning.ForvaltningMellomlagringRest;
import no.nav.foreldrepenger.soknad.server.konfig.swagger.ForvaltningOpenApiConfig;
import no.nav.foreldrepenger.soknad.server.sikkerhet.AuthenticationFilter;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends ResourceConfig {
    public static final String API_URI ="/forvaltning/api";

    public ForvaltningApiConfig() {
        setApplicationName(ForvaltningApiConfig.class.getSimpleName());
        register(AuthenticationFilter.class); // Sikkerhet
        register(GeneralRestExceptionMapper.class); // Exception handling
        register(ValidationExceptionMapper.class); // Exception handling
        register(JacksonJsonConfig.class); // Json
        registerOpenApi();
        registerClasses(getForvaltningKlasser());
        setProperties(getApplicationProperties());
    }

    public static Set<Class<?>> getForvaltningKlasser() {
        return Set.of(
            ProsessTaskRestTjeneste.class,
            ForvaltningMellomlagringRest.class
        );
    }

    private void registerOpenApi() {
        ForvaltningOpenApiConfig.registerOpenApi(this);
        register(OpenApiResource.class);
    }
}
