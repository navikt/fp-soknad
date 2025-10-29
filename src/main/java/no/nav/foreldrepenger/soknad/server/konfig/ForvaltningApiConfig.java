package no.nav.foreldrepenger.soknad.server.konfig;

import static no.nav.foreldrepenger.soknad.server.konfig.ApiConfig.getApplicationProperties;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.server.JacksonJsonConfig;
import no.nav.foreldrepenger.soknad.server.error.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.soknad.server.error.ValidationExceptionMapper;
import no.nav.foreldrepenger.soknad.server.forvaltning.ForvaltningMellomlagringRest;
import no.nav.foreldrepenger.soknad.server.sikkerhet.AuthenticationFilter;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends ResourceConfig {
    public static final String API_URI ="/forvaltning/api";
    private static final Environment ENV = Environment.current();
    private static final String SWAGGER_ID = "openapi.context.id.servlet." + ForvaltningApiConfig.class.getName();

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
        var oas = new OpenAPI()
            .openapi("3.1.1")
            .info(new Info()
                .title("FPSOKNAD - søknad og ettersendelser (frontend)")
                .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
                .description("REST grensesnitt for Frontend."))
            .addServersItem(new Server().url(ENV.getProperty("context.path", "/fpsoknad")));
        var oasConfig =  new SwaggerConfiguration()
            .id(SWAGGER_ID)
            .openAPI(oas)
            .prettyPrint(true)
            .resourceClasses(getForvaltningKlasser().stream().map(Class::getName).collect(Collectors.toSet()));
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
