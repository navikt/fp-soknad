package no.nav.foreldrepenger.soknad.server.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.innsending.SøknadRest;
import no.nav.foreldrepenger.soknad.mellomlagring.MellomlagringRest;
import no.nav.foreldrepenger.soknad.server.konfig.swagger.TypegenereringFrontendOpenApiReader;
import no.nav.vedtak.openapi.OpenApiUtils;
import no.nav.vedtak.server.rest.FeilUtils;
import no.nav.vedtak.server.rest.FpRestJackson2Feature;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends ResourceConfig {

    public static final String API_URI = "/api";
    private static final Environment ENV = Environment.current();
    private static final Logger SECURE_LOG = LoggerFactory.getLogger("secureLogger");

    public ApiConfig() {
        // Nesten standard FpRestJackson2-oppsett, men lokale tilpasninger av exceptions.
        register(FpRestJackson2Feature.class); // Jackson konfigurasjon
        FeilUtils.setSikkerlogg(SECURE_LOG);  // Sørger for logging av feil (validering og annet)  til sikkerlogg
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
