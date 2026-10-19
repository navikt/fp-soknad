package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(
    tokenConfig = TokenFlow.AZUREAD_CC,
    endpointProperty = "fpoversikt.base.url",
    endpointDefault = "http://fpoversikt.teamforeldrepenger/fpoversikt",
    scopesProperty = "fpoversikt.scopes",
    scopesDefault = "api://prod-gcp.teamforeldrepenger.fpoversikt/.default"
)
public class FpOversiktRestKlient implements FpOversiktTjeneste {

    private static final String SØKNAD_PATH = "/api/søknad";

    private final RestClient klient;
    private final RestConfig restConfig;
    private final URI endpoint;

    public FpOversiktRestKlient() {
        this(RestClient.client());
    }

    public FpOversiktRestKlient(RestClient klient) {
        this.klient = klient;
        this.restConfig = RestConfig.forClient(FpOversiktRestKlient.class);
        this.endpoint = UriBuilder.fromUri(restConfig.endpoint()).path(SØKNAD_PATH).build();
    }

    @Override
    public void send(OversiktSøknadDto søknad) {
        var request = RestRequest.newPOSTJson(søknad, endpoint, restConfig);
        klient.sendReturnOptional(request, String.class);
    }
}
