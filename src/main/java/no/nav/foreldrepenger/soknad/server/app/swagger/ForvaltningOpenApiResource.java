package no.nav.foreldrepenger.soknad.server.app.swagger;

import static no.nav.foreldrepenger.soknad.server.app.ForvaltningApiConfig.getForvaltningKlasser;

import io.swagger.v3.oas.integration.api.OpenApiContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/openapi.{type:json|yaml}")
public class ForvaltningOpenApiResource {
    private static final String SWAGGER_ID = "forvaltning";
    private static final OpenApiContext openApiContext = new OpenApiKonfigurasjon(SWAGGER_ID)
        .resourceClasses(getForvaltningKlasser())
        .buildContext();

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    public Response getOpenApi(@PathParam("type") String type) throws Exception {
        var oas = openApiContext.read();
        if ("yaml".equalsIgnoreCase(type)) {
            return Response.ok(openApiContext.getOutputYamlMapper().writeValueAsString(oas))
                .type("application/yaml")
                .build();
        } else {
            return Response.ok(openApiContext.getOutputJsonMapper().writeValueAsString(oas))
                .type(MediaType.APPLICATION_JSON)
                .build();
        }
    }
}
