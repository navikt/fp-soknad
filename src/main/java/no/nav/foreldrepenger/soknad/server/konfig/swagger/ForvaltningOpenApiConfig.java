package no.nav.foreldrepenger.soknad.server.konfig.swagger;

import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.soknad.server.konfig.ForvaltningApiConfig;

public class ForvaltningOpenApiConfig extends OpenApiContextBuilder {
    private static final Set<Class<?>> INKLUDER_CLASSER = ForvaltningApiConfig.getForvaltningKlasser();

    private ForvaltningOpenApiConfig() {
        // Hide
    }

    public static void registerOpenApi(Application application) {
        var oasConfig = buildOpenApiConfig("Fpsoknad - søknad og ettersendelser (frontend)", application)
            .readerClass(ForvaltningOpenApiConfig.OpenApiReader.class.getName());
        buildOpenApiContext(application, oasConfig);
    }

    public static class OpenApiReader extends Reader {
        @Override
        public OpenAPI read(Set<Class<?>> resourceClasses) {
            return super.read(resourceClasses.stream()
                .filter(INKLUDER_CLASSER::contains)
                .collect(Collectors.toSet()));
        }
    }
}
