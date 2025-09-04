package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

import java.net.URI;
import java.util.Optional;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPSAK)
public class FpsakRestKlient implements FpsakTjeneste {

    private static final String JOURNALPOSTTILKNYTNING_PATH = "/api/fordel/fagsak/knyttJournalpost";
    private static final String FAGSAKINFORMASJON_PATH = "/api/fordel/fagsak/informasjon";
    private static final String FAGSAK_OPPRETT_PATH = "/api/fordel/fagsak/opprett";
    private static final String VURDER_FAGSYSTEM_PATH = "/api/fordel/vurderFagsystem";


    private final URI knytningEndpoint;
    private final URI fagsakinfoEndpoint;
    private final URI opprettsakEndpoint;
    private final URI fagsystemEndpoint;

    private final RestClient restKlient;
    private final RestConfig restConfig;

    public FpsakRestKlient() {
        this.restKlient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        var endpoint = restConfig.fpContextPath();
        this.knytningEndpoint = lagURI(endpoint, JOURNALPOSTTILKNYTNING_PATH);
        this.fagsakinfoEndpoint = lagURI(endpoint, FAGSAKINFORMASJON_PATH);
        this.opprettsakEndpoint = lagURI(endpoint, FAGSAK_OPPRETT_PATH);
        this.fagsystemEndpoint = lagURI(endpoint, VURDER_FAGSYSTEM_PATH);
    }

    @Override
    public VurderFagsystemResultat vurderFagsystem() {
        var dto = new VurderFagsystemDto();
        // TODO: Set fiels basert på søknad som sendes inn + metadata
        // Fjernet klage endepunkt, ettersom vi ikke skal motta klager her => Går alltid mot fagsystemEndpoint
        var request = RestRequest.newPOSTJson(dto, fagsystemEndpoint, restConfig);
        var respons = restKlient.send(request, BehandlendeFagsystemDto.class);
        return VurderFagsystemResultat.fra( respons);
    }

    @Override
    public Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto) {
        var request = RestRequest.newPOSTJson(saksnummerDto, fagsakinfoEndpoint, restConfig);
        var info = restKlient.send(request, FagsakInfomasjonDto.class);
        return Optional.ofNullable(info);
    }

    @Override
    public SaksnummerDto opprettSak(OpprettSakDto opprettSakDto) {
        var request = RestRequest.newPOSTJson(opprettSakDto, opprettsakEndpoint, restConfig);
        return restKlient.send(request, SaksnummerDto.class);
    }

    @Override
    public void knyttSakOgJournalpost(JournalpostKnyttningDto dto) {
        var request = RestRequest.newPOSTJson(dto, knytningEndpoint, restConfig);
        restKlient.sendReturnOptional(request, String.class);
    }

    private URI lagURI(URI context, String api) {
        return UriBuilder.fromUri(context).path(api).build();
    }
}
