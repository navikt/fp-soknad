package no.nav.foreldrepenger.soknad.server.forvaltning;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@OpenAPIDefinition(tags = @Tag(name = "soknad", description = "Forvaltningstjeneste for søknader"))
@Path("/soknad")
@ApplicationScoped
public class ForvaltningSoknadRest {
    private static final JsonMapper MAPPER = DefaultJsonMapper.getJsonMapper();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private DokumentRepository dokumentRepository;


    public ForvaltningSoknadRest() {
        // CDI
    }

    @Inject
    public ForvaltningSoknadRest(DokumentRepository dokumentRepository) {
        this.dokumentRepository = dokumentRepository;
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagre korrigert versjon av søknad-json. Støtter ikke endret søknadstype.", tags = "soknad", responses = {
        @ApiResponse(responseCode = "200", description = "Lagret søknad-json"),
        @ApiResponse(responseCode = "400", description = "Feil i request"),
        @ApiResponse(responseCode = "404", description = "Fant ikke søknad for forsendelse"),
    })
    @Transactional
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.DRIFT, sporingslogg = true)
    public Response patchSoknad(@TilpassetAbacAttributt(supplierClass = ForsendelseIdFnrSupplier.class)
                                @FormParam("fødselsnummer") @Valid @NotNull Fødselsnummer fødselsnummer,
                                @FormParam("forsendelseId") @Valid @NotNull UUID forsendelseId,
                                @FormParam("soknadJson") String json) {
        var fnrForsendelser = dokumentRepository.hentForsendelse(fødselsnummer.value());
        boolean forsendelseMatcherFnr = fnrForsendelser.stream().anyMatch(f -> fødselsnummer.value().equals(f.getBrukersFnr()));
        if (!forsendelseMatcherFnr) {
            throw new BadRequestException("fnr/forsendelseId mismatch");
        }
        var søknadDokument = dokumentRepository.hentSøknadDokument(forsendelseId).orElseThrow();
        var nySøknadJson = deseraliserOgValiderSøknad(json, søknadDokument.getDokumentTypeId());
        dokumentRepository.oppdaterSøknadJson(søknadDokument, serialiser(nySøknadJson));
        return Response.ok().build();
    }

    private SøknadDto deseraliserOgValiderSøknad(String json, DokumentTypeId dokumentTypeId) {
        try {
            var søknad = SøknadJsonMapper.deseraliserSøknad(json.getBytes(StandardCharsets.UTF_8), dokumentTypeId);
            var violations = VALIDATOR.validate(søknad);
            if (!violations.isEmpty()) {
                var feilmelding = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining(", "));
                throw new BadRequestException("soknadJson validerer ikke som " + søknad.getClass().getSimpleName() + ": " + feilmelding);
            }
            return søknad;
        } catch (IOException e) {
            throw new BadRequestException("soknadJson må være gyldig JSON for dokumentTypeId " + dokumentTypeId, e);
        }
    }

    private static byte[] serialiser(SøknadDto søknad) {
        try {
            return MAPPER.writeValueAsBytes(søknad);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Kunne ikke serialisere validert soknadJson", e);
        }
    }

    public static class ForsendelseIdFnrSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var forsendelseId = (UUID) obj;
            var dokumentRepository = CDI.current().select(DokumentRepository.class).get();
            var metadata = dokumentRepository.hentUnikDokumentMetadata(forsendelseId)
                .orElseThrow(() -> new NotFoundException("Fant ikke forsendelse " + forsendelseId));

            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.FNR, metadata.getBrukersFnr());
        }
    }

}
