package no.nav.foreldrepenger.soknad.server.app;

import static no.nav.foreldrepenger.soknad.server.app.ApiConfig.getApplicationProperties;

import java.util.Optional;
import java.util.Set;

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
import no.nav.foreldrepenger.soknad.server.AuthenticationFilter;
import no.nav.foreldrepenger.soknad.server.JacksonJsonConfig;
import no.nav.foreldrepenger.soknad.server.error.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.soknad.server.error.ValidationExceptionMapper;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends ResourceConfig {
    public static final String API_URI ="/forvaltning/api";
    private static final Environment ENV = Environment.current();

    public ForvaltningApiConfig() {
        register(AuthenticationFilter.class); // Sikkerhet
        register(GeneralRestExceptionMapper.class); // Exception handling
        register(ValidationExceptionMapper.class); // Exception handling
        register(JacksonJsonConfig.class); // Json

        registerOpenApi();
        register(ProsessTaskRestTjeneste.class);
        setProperties(getApplicationProperties());
    }

    private void registerOpenApi() {
        var oas = new OpenAPI();
        var info = new Info()
            .title("FPSOKNAD - søknad og ettersendelser")
            .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
            .description("REST grensesnitt for FPSOKNAD.");

        oas.info(info).addServersItem(new Server().url(ENV.getProperty("context.path", "/fpsoknad")));
        var oasConfig = new SwaggerConfiguration()
            .openAPI(oas)
            .prettyPrint(true)
            .resourceClasses(Set.of(ProsessTaskRestTjeneste.class.getName()))
            .readerClass(OpenApiReaderTypeGeneringFrontend.class.getName()); // For autogenering av typer i frontend

        var context = new JaxrsOpenApiContextBuilder<>();
        context.setOpenApiConfiguration(oasConfig);
        context.setApplication(getApplication());

        try {
            context.buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }

        register(OpenApiResource.class);
    }
}
