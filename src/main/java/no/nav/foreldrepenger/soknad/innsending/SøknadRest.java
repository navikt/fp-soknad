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
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.foreldrepenger.common.domain.Saksnummer;
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
    private TilgangskontrollTjeneste tilgangskontrollTjeneste;
    private InnloggetBruker innloggetBruker;

    public SøknadRest() {
        // CDI
    }

    @Inject
    public SøknadRest(SøknadInnsendingTjeneste søknadInnsendingTjeneste,
                      StatusInnsendingTjeneste statusInnsendingTjeneste,
                      TilgangskontrollTjeneste tilgangskontrollTjeneste,
                      InnloggetBruker innloggetBruker) {
        this.søknadInnsendingTjeneste = søknadInnsendingTjeneste;
        this.statusInnsendingTjeneste = statusInnsendingTjeneste;
        this.tilgangskontrollTjeneste = tilgangskontrollTjeneste;
        this.innloggetBruker = innloggetBruker;
    }

    @POST
    @Path("/foreldrepenger")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull ForeldrepengesøknadDto foreldrepengesøknadDto) {
        tilgangskontrollTjeneste.validerSøkerFraKontekstErSammeSomSøknad(foreldrepengesøknadDto.søkerinfo().fnr());
        søknadInnsendingTjeneste.lagreSøknadInnsending(foreldrepengesøknadDto);
        return Response.ok().build();
    }

    @POST
    @Path("/engangsstonad")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull EngangsstønadDto engangsstønadDto) {
        tilgangskontrollTjeneste.validerSøkerFraKontekstErSammeSomSøknad(engangsstønadDto.søkerinfo().fnr());
        søknadInnsendingTjeneste.lagreSøknadInnsending(engangsstønadDto);
        return Response.ok().build();
    }

    @POST
    @Path("/svangerskapspenger")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull SvangerskapspengesøknadDto svangerskapspengesøknadDto) {
        tilgangskontrollTjeneste.validerSøkerFraKontekstErSammeSomSøknad(svangerskapspengesøknadDto.søkerinfo().fnr());
        søknadInnsendingTjeneste.lagreSøknadInnsending(svangerskapspengesøknadDto);
        return Response.ok().build();
    }

    @POST
    @Path("/foreldrepenger/endre")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull EndringssøknadForeldrepengerDto endringssøknadForeldrepengerDto) {
        tilgangskontrollTjeneste.validerSøkerFraKontekstErSammeSomSøknad(endringssøknadForeldrepengerDto.søkerinfo().fnr());
        tilgangskontrollTjeneste.validerSaksnummerKnyttetTilSøker(endringssøknadForeldrepengerDto.saksnummer());
        søknadInnsendingTjeneste.lagreSøknadInnsending(endringssøknadForeldrepengerDto);
        return Response.ok().build();
    }


    @POST
    @Path("/ettersend")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(@Valid @NotNull EttersendelseDto ettersendelseDto) {
        tilgangskontrollTjeneste.validerSaksnummerKnyttetTilSøker(ettersendelseDto.saksnummer());
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
