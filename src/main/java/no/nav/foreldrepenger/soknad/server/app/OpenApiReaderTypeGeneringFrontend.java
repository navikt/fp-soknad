package no.nav.foreldrepenger.soknad.server.app;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.TypeNameResolver;
import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import jakarta.ws.rs.Path;
import no.nav.openapi.spec.utils.openapi.DiscriminatorModelConverter;
import no.nav.openapi.spec.utils.openapi.EnumVarnamesConverter;
import no.nav.openapi.spec.utils.openapi.JsonSubTypesModelConverter;
import no.nav.openapi.spec.utils.openapi.NoJsonSubTypesAnnotationIntrospector;
import no.nav.openapi.spec.utils.openapi.RefToClassLookup;

/**
 * OpenAPI / Swagger
 * Custom OpenApiReader for å eksludere paths til endepunkt i ApiConfig.
 * Schema for endepunkt i api vil bli tilgjengliggjort, men ikke mulig å kjøre swagger mot de.
 * Schema brukes til å autogenerere typer i frontend
 */
public class OpenApiReaderTypeGeneringFrontend extends Reader {
    private static final Set<Class<?>> API_SKAL_SKAL_INKLUDERES = ApiConfig.getApplicationClasses();
    private static final Set<String> IGNORE_PATHS = API_SKAL_SKAL_INKLUDERES.stream()
        .map(klasse -> klasse.getAnnotation(Path.class))
        .map(Path::value)
        .collect(Collectors.toSet());

    @Override
    public OpenAPI read(Set<Class<?>> resourceClasses) {
        lagCustomModelConvertersForFrontendTypegenerering();
        var forvaltningsApiOgAppliactionsApi = Stream.concat(
            resourceClasses.stream(),
            API_SKAL_SKAL_INKLUDERES.stream()
        ).collect(Collectors.toSet());
        var openApi = super.read(forvaltningsApiOgAppliactionsApi);

        // Ignorer paths fra IGNORE_PATHS
        var filtered = new Paths();
        openApi.getPaths().forEach((path, item) -> {
            if (IGNORE_PATHS.stream().noneMatch(path::contains)) {
                filtered.addPathItem(path, item);
            }
        });
        openApi.setPaths(filtered);
        return openApi;
    }

    private static void lagCustomModelConvertersForFrontendTypegenerering() {
        // Påfølgende ModelConverts oppsett er tilpasset fra K9 sin openapi-spec-utils: https://github.com/navikt/openapi-spec-utils

        // Denne gjør at enums trekkes ut som egne typer istedenfor inline
        ModelResolver.enumsAsRef = true;
        ModelConverters.reset();
        var typeNameResolver = TypeNameResolver.std;
        typeNameResolver.setUseFqn(true);

        ModelConverters.getInstance().addConverter(new ModelResolver(lagObjectMapperUtenJsonSubTypeAnnotasjoner(),  typeNameResolver));
        ModelConverters.getInstance().addConverter(new JsonSubTypesModelConverter());
        ModelConverters.getInstance().addConverter(new DiscriminatorModelConverter(new RefToClassLookup()));
        ModelConverters.getInstance().addConverter(new EnumVarnamesConverter());
    }

    private static ObjectMapper lagObjectMapperUtenJsonSubTypeAnnotasjoner() {
        final var om = JsonMapper.builder(ObjectMapperFactory.createJson().getFactory())
            // OpenApi-spec som blir generert er ikke alltid konsekvent på rekkefølgen til properties.
            // Ved å skru på disse flaggene blir output deterministic og det blir enklere å se hva som faktisk er diff fra forrige typegenerering
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .enable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();
        // Fjern alle annotasjoner om JsonSubTypes. Hvis disse er med i generasjon av openapi spec får vi sirkulære avhengigheter.
        // Det skjer ved at superklassen sier den har "oneOf" arvingene sine. Mens en arving sier den har "allOf" forelderen sin.
        // Ved å fjerne jsonSubType annotasjoner får vi heller en enveis-lenke der superklassen definerer arvingene sine med "oneOf".
        om.setAnnotationIntrospector(new NoJsonSubTypesAnnotationIntrospector());
        return om;
    }

}
