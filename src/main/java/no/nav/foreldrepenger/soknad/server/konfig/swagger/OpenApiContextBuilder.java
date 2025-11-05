package no.nav.foreldrepenger.soknad.server.konfig.swagger;

import java.util.Optional;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;

abstract class OpenApiContextBuilder {
    private static final Environment ENV = Environment.current();

    static SwaggerConfiguration buildOpenApiConfig(String tittel, Application application) {
        var oas = new OpenAPI()
            .openapi("3.1.1")
            .info(new Info()
                .title(tittel)
                .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
                .description("REST grensesnitt for Frontend."))
            .addServersItem(new Server().url(ENV.getProperty("context.path", "/fpsoknad")));
        var oasConfig =  new SwaggerConfiguration()
            .id(idFra(application))
            .openAPI(oas)
            .prettyPrint(true);
        return oasConfig;
    }

    static void buildOpenApiContext(Application application, OpenAPIConfiguration oasConfig) {
        try {
            new JaxrsOpenApiContextBuilder<>()
                .ctxId(idFra(application))
                .application(application)
                .openApiConfiguration(oasConfig)
                .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    private static String idFra(Application application) {
        return "openapi.context.id.servlet." + application.getClass().getName();
    }
}
