package no.nav.foreldrepenger.mottak.domene.server;

import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.mottak.domene.innsending.SøknadRest;
import no.nav.foreldrepenger.mottak.domene.mellomlagring.MellomlagringRest;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    public static final String API_URI ="/api";

    private static final Environment ENV = Environment.current();

    public ApiConfig() {
        var oas = new OpenAPI();
        var info = new Info()
            .title("FPOVERSIKT - saksoversikt")
            .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
            .description("REST grensesnitt for FPOVERSIKT.");

        oas.info(info).addServersItem(new Server().url("/"));
        var oasConfig = new SwaggerConfiguration()
            .openAPI(oas)
            .prettyPrint(true)
            .resourceClasses(Set.of(ProsessTaskRestTjeneste.class.getName()));

        try {
            new GenericOpenApiContextBuilder<>()
                .openApiConfiguration(oasConfig)
                .buildContext(true)
                .read();
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        // eksponert grensesnitt bak sikkerhet. Nå er vi på max Set.of før varargs-versjonen.
        return Set.of(
            SøknadRest.class,
            MellomlagringRest.class,
            MultiPartFeature.class
        );
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }

}
