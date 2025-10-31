package no.nav.foreldrepenger.soknad.server.app.swagger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;

public class OpenApiKonfigurasjon {
    private static final Environment ENV = Environment.current();
    private final String swaggerId;
    private final SwaggerConfiguration openApiConfig;

    public OpenApiKonfigurasjon(String id, String pathForvaltning) {
        this.swaggerId = id;
        this.openApiConfig = lagOpenApiKonfigurasjon(pathForvaltning);
    }

    public OpenApiKonfigurasjon resourceClasses(Set<Class<?>> resourceClasses) {
        openApiConfig.resourceClasses(resourceClasses.stream().map(Class::getName).collect(Collectors.toSet()));
        return this;
    }

    public OpenApiKonfigurasjon readerClass(Class<? extends Reader> reader) {
        openApiConfig.readerClass(reader.getName());
        return this;
    }

    private SwaggerConfiguration lagOpenApiKonfigurasjon(String pathForvaltning) {
        OpenAPI oas = new OpenAPI()
            .openapi("3.1.1")
            .info(new Info()
                .title("FPSOKNAD - søknad og ettersendelser (frontend)")
                .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
                .description("REST grensesnitt for Frontend."));
        if (pathForvaltning != null) {
            oas.addServersItem(new Server().url(ENV.getProperty("context.path", "/fpsoknad") + pathForvaltning));
        }
        return new SwaggerConfiguration()
            .id(swaggerId)
            .openAPI(oas)
            .prettyPrint(true);
    }

    public OpenApiContext buildContext() {
        try {
            return new JaxrsOpenApiContextBuilder<>()
                .ctxId(swaggerId)
                .openApiConfiguration(openApiConfig)
                .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

}
