package no.nav.foreldrepenger.mottak.domene.innsending;


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
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.endringssøknad.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.engangsstønad.EngangsstønadDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.ettersendelse.EttersendelseDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.svangerskapspenger.SvangerskapspengesøknadDto;

@Path("/soknad")
@ApplicationScoped
@Transactional
public class SøknadRest {

    @Inject
    public SøknadRest() {
        // CDI
    }

    @POST
    @Path("/foreldrepenger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid @NotNull ForeldrepengesøknadDto foreldrepengesøknadDto) {

    }

    @POST
    @Path("/engangsstonad")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid @NotNull EngangsstønadDto engangsstønadDto) {
    }

    @POST
    @Path("/svangerskapspenger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid @NotNull SvangerskapspengesøknadDto svangerskapspengesøknadDto) {
    }

    @POST
    @Path("/endre/foreldrepenger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid @NotNull EndringssøknadForeldrepengerDto endringssøknadForeldrepengerDto) {
    }


    @POST
    @Path("/ettersend")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid @NotNull EttersendelseDto ettersendelseDto) {
    }

}
