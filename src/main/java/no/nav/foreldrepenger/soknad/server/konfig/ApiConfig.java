package no.nav.foreldrepenger.soknad.server.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.innsending.SøknadRest;
import no.nav.foreldrepenger.soknad.mellomlagring.MellomlagringRest;
import no.nav.foreldrepenger.soknad.server.error.LokalRestExceptionMapper;
import no.nav.foreldrepenger.soknad.server.error.LokalValidationExceptionMapper;
import no.nav.foreldrepenger.soknad.server.konfig.swagger.TypegenereringFrontendOpenApiReader;
import no.nav.vedtak.openapi.OpenApiUtils;
import no.nav.vedtak.server.rest.AuthenticationFilter;
import no.nav.vedtak.server.rest.jackson.Jackson2MapperFeature;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends ResourceConfig {

    public static final String API_URI = "/api";
    private static final Environment ENV = Environment.current();

    public ApiConfig() {
        // Nesten standard FpRestJackson2-oppsett, men lokale tilpasninger av exceptions.
        register(Jackson2MapperFeature.class); // Jackson konfigurasjon
        register(AuthenticationFilter.class); // Autentisering
        register(LokalRestExceptionMapper.class); // Exception handling
        register(LokalValidationExceptionMapper.class); // Exception handling
        register(MultiPartFeature.class); // Multipart upload mellomlagring
        if (!ENV.isProd()) {
            registerOpenApi();
        }
        registerClasses(getApplicationClasses());
        setProperties(getApplicationProperties());
    }

    private static Set<Class<?>> getApplicationClasses() {
        return Set.of(SøknadRest.class, MellomlagringRest.class);
    }


    static Map<String, Object> getApplicationProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }

    private void registerOpenApi() {
        var contextPath = ENV.getProperty("context.path", "/fpsoknad");
        OpenApiUtils.openApiConfigFor("Fpsoknad - specifikasjon for typegenerering frontend", contextPath, this)
            .readerClass(TypegenereringFrontendOpenApiReader.class)
            .registerClasses(getApplicationClasses())
            .buildOpenApiContext();
        register(OpenApiResource.class);
    }
}
