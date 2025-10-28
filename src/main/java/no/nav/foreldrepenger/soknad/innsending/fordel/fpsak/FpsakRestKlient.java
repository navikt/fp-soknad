package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import java.net.URI;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPSAK)
public class FpsakRestKlient implements FpsakTjeneste {

    private static final String FAGSAKINFORMASJON_PATH = "/api/fordel/fagsak/informasjon";
    private static final String VURDER_FAGSYSTEM_PATH = "/api/fordel/vurderFagsystem";
    private static final String MOTTAK_JOURNALPOST_PATH = "/api/fordel/journalpost";

    private final URI fagsakinfoEndpoint;
    private final URI fagsystemEndpoint;
    private final URI journalpostEndpoint;

    private final RestClient restKlient;
    private final RestConfig restConfig;

    public FpsakRestKlient() {
        this.restKlient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        var endpoint = restConfig.fpContextPath();
        this.fagsakinfoEndpoint = lagURI(endpoint, FAGSAKINFORMASJON_PATH);
        this.fagsystemEndpoint = lagURI(endpoint, VURDER_FAGSYSTEM_PATH);
        this.journalpostEndpoint = lagURI(endpoint, MOTTAK_JOURNALPOST_PATH);
    }

    @Override
    public VurderFagsystemResultat vurderFagsystem(VurderFagsystemDto vurderFagsystemDto) {
        var request = RestRequest.newPOSTJson(vurderFagsystemDto, fagsystemEndpoint, restConfig);
        var respons = restKlient.send(request, BehandlendeFagsystemDto.class);
        return VurderFagsystemResultat.fra(respons);
    }

    @Override
    public Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto) {
        var request = RestRequest.newPOSTJson(saksnummerDto, fagsakinfoEndpoint, restConfig);
        var info = restKlient.send(request, FagsakInfomasjonDto.class);
        return Optional.ofNullable(info);
    }

    @Override
    public void sendOgKnyttJournalpost(JournalpostMottakDto journalpost) {
        var request = RestRequest.newPOSTJson(journalpost, journalpostEndpoint, restConfig);
        restKlient.sendReturnOptional(request, String.class);
    }

    private URI lagURI(URI context, String api) {
        return UriBuilder.fromUri(context).path(api).build();
    }
}
