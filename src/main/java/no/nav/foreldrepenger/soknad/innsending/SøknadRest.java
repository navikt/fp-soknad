package no.nav.foreldrepenger.soknad.innsending;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.EttersendelseDto;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;

@Path("/soknad")
@ApplicationScoped
@Transactional
public class SøknadRest {

    private SøknadInnsendingTjeneste søknadInnsendingTjeneste;
    private StatusInnsendingTjeneste statusInnsendingTjeneste;
    private InnloggetBruker innloggetBruker;

    public SøknadRest() {
        // CDI
    }

    @Inject
    public SøknadRest(SøknadInnsendingTjeneste søknadInnsendingTjeneste, StatusInnsendingTjeneste statusInnsendingTjeneste,
                      InnloggetBruker innloggetBruker) {
        this.søknadInnsendingTjeneste = søknadInnsendingTjeneste;
        this.statusInnsendingTjeneste = statusInnsendingTjeneste;
        this.innloggetBruker = innloggetBruker;
    }

    @POST
    @Path("/foreldrepenger")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull ForeldrepengesøknadDto foreldrepengesøknadDto) {
        // TODO: Sjekk at søker i objekt er samme som søker i subjekt i tokenet.
        søknadInnsendingTjeneste.lagreSøknadInnsending(foreldrepengesøknadDto);
        return Response.ok().build();
    }

    @POST
    @Path("/engangsstonad")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull EngangsstønadDto engangsstønadDto) {
        søknadInnsendingTjeneste.lagreSøknadInnsending(engangsstønadDto);
        return Response.ok().build();
    }

    @POST
    @Path("/svangerskapspenger")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull SvangerskapspengesøknadDto svangerskapspengesøknadDto) {
        søknadInnsendingTjeneste.lagreSøknadInnsending(svangerskapspengesøknadDto);
        return Response.ok().build();
    }

    @POST
    @Path("/foreldrepenger/endre")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull EndringssøknadForeldrepengerDto endringssøknadForeldrepengerDto) {
        // TODO: Valider at søker er søker i oppgitt fagsak. Slå opp fagsakinfo fra fpsak og valider aktørid er like aktørid til søker. Feil hardt hvis ikke!
        søknadInnsendingTjeneste.lagreSøknadInnsending(endringssøknadForeldrepengerDto);
        return Response.ok().build();
    }


    @POST
    @Path("/ettersend")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull EttersendelseDto ettersendelseDto) {
        // TODO: Valider at søker er søker i oppgitt fagsak. Slå opp fagsakinfo fra fpsak og valider aktørid er like aktørid til søker. Feil hardt hvis ikke!
        søknadInnsendingTjeneste.lagreEttersendelseInnsending(ettersendelseDto);
        return Response.ok().build();
    }


    @GET
    @Path("/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response status() {
        var fnr = innloggetBruker.brukerFraKontekst();

        var status = statusInnsendingTjeneste.status(fnr);
        if (status.isEmpty()) {
            return Response.status(404).build();
        }

        return Response.ok(status).build();
    }
}
