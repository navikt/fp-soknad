package no.nav.foreldrepenger.soknad.server.forvaltning;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.kontrakter.fpsoknad.Fødselsnummer;
import no.nav.foreldrepenger.soknad.mellomlagring.GCPMellomlagring;
import no.nav.foreldrepenger.soknad.mellomlagring.InMemoryMellomlagring;
import no.nav.foreldrepenger.soknad.mellomlagring.KrypteringHjelper;
import no.nav.foreldrepenger.soknad.mellomlagring.Mellomlagring;
import no.nav.foreldrepenger.soknad.mellomlagring.MellomlagringTjeneste;
import no.nav.foreldrepenger.soknad.mellomlagring.YtelseMellomlagringType;
import no.nav.vedtak.felles.prosesstask.rest.AbacEmptySupplier;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@OpenAPIDefinition(tags = @Tag(name = "mellomlagring", description = "Forvaltnignstjeneste for mellomlagring"))
@Path("/mellomlagring")
@ApplicationScoped
public class ForvaltningMellomlagringRest {
    private static final Environment ENV = Environment.current();
    private Mellomlagring mellomlagring;
    private String krypteringsnøkkel;


    public ForvaltningMellomlagringRest() {
        // CDI
    }

    @Inject
    public ForvaltningMellomlagringRest(@KonfigVerdi(value = "KRYPTERING_PASSWORD") String krypteringsnøkkel, GCPMellomlagring mellomlagring) {
        this.mellomlagring = ENV.isLocal() ? InMemoryMellomlagring.getInstance() : mellomlagring;
        this.krypteringsnøkkel = krypteringsnøkkel;
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent mellomlagret søkand for angitt bruker", tags = "mellomlagring", responses = {
        @ApiResponse(responseCode = "200", description = "Mellomlagring returneres"),
        @ApiResponse(responseCode = "204", description = "Finnes ingen mellomlagring"),
    })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response hentMellomlagring(@TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class)
                                      @FormParam("fødselsnummer") @Valid @NotNull Fødselsnummer fødselsnummer,
                                      @FormParam("ytelse") YtelseMellomlagringType ytelse) {
        var krypto = new KrypteringHjelper(krypteringsnøkkel, fødselsnummer.value());
        var mappenavn = krypto.uniktMappenavn(fødselsnummer.value(), ytelse, false);
        var mellomlagretSøknad = mellomlagring.les(mappenavn, MellomlagringTjeneste.SØKNAD);
        if (mellomlagretSøknad.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(krypto.decrypt(mellomlagretSøknad.get())).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Sletter mellomlagret søknad for angitt bruker", tags = "mellomlagring", responses = {
        @ApiResponse(responseCode = "204", description = "Mellomlagring funnet og slettet på bruker"),
        @ApiResponse(responseCode = "404", description = "Finnes ingen mellomlagring på bruker"),
    })
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.DRIFT, sporingslogg = false)
    public Response slettMellomlagringPåBruker(@TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class)
                                               @FormParam("fødselsnummer") @Valid @NotNull Fødselsnummer fødselsnummer,
                                               @FormParam("ytelse") YtelseMellomlagringType ytelse) {
        var krypto = new KrypteringHjelper(krypteringsnøkkel, fødselsnummer.value());
        var mappenavn = krypto.uniktMappenavn(fødselsnummer.value(), ytelse, false);
        var mellomlagretSøknad = mellomlagring.les(mappenavn, MellomlagringTjeneste.SØKNAD);
        if (mellomlagretSøknad.isEmpty()) {
            return Response.status(404, "Fant ingen mellomlagring påMellomlagring på bruker finnes ikke.").build();
        }
        this.mellomlagring.slett(mappenavn, MellomlagringTjeneste.SØKNAD);
        return Response.noContent().build();
    }
}
