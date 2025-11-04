package no.nav.foreldrepenger.soknad.server.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.innsending.SøknadRest;
import no.nav.foreldrepenger.soknad.mellomlagring.MellomlagringRest;
import no.nav.foreldrepenger.soknad.server.JacksonJsonConfig;
import no.nav.foreldrepenger.soknad.server.error.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.soknad.server.error.ValidationExceptionMapper;
import no.nav.foreldrepenger.soknad.server.sikkerhet.AuthenticationFilter;
import no.nav.vedtak.exception.TekniskException;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends ResourceConfig {

    public static final String API_URI ="/api";
    private static final Environment ENV = Environment.current();
    private static final String SWAGGER_ID = "openapi.context.id.servlet." + ApiConfig.class.getName();

    public ApiConfig() {
        setApplicationName(ApiConfig.class.getSimpleName());
        register(AuthenticationFilter.class); // Sikkerhet
        register(GeneralRestExceptionMapper.class); // Exception handling
        register(ValidationExceptionMapper.class); // Exception handling
        register(JacksonJsonConfig.class); // Json
        register(MultiPartFeature.class); // Multipart upload mellomlagring
        if (!ENV.isProd()) {
            registerOpenApi(); // Brukes til typegenerering frontend
        }
        registerClasses(getApplicationClasses());
        setProperties(getApplicationProperties());
    }

    public static Set<Class<?>> getApplicationClasses() {
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
        var oas = new OpenAPI()
            .openapi("3.1.1")
            .info(new Info()
                .title("Fpsoknad - specifikasjon for typegenerering frontend")
                .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
                .description("REST grensesnitt for Frontend."))
            .addServersItem(new Server().url(ENV.getProperty("context.path", "/fpsoknad")));
        var oasConfig =  new SwaggerConfiguration()
            .id(SWAGGER_ID)
            .openAPI(oas)
            .prettyPrint(true)
            .resourceClasses(getApplicationClasses().stream().map(Class::getName).collect(Collectors.toSet()))
            .readerClass(TypegenereringFrontendOpenApiReader.class.getName());
        try {
            new JaxrsOpenApiContextBuilder<>()
                .ctxId(SWAGGER_ID)
                .application(this)
                .openApiConfiguration(oasConfig)
                .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
        register(OpenApiResource.class);
    }
}
