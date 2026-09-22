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
    endpointDefault = "http://fpoversikt/fpoversikt",
    scopesProperty = "fpoversikt.scopes",
    scopesDefault = "api://prod-gcp.teamforeldrepenger.fpoversikt/.default")
public class FpoversiktRestKlient implements FpoversiktTjeneste {

    private static final String ANNEN_PART_UTTAKSPLAN_PATH = "/api/uttaksplan/annen-part";

    private final RestClient restKlient;
    private final RestConfig restConfig;
    private final URI endpoint;

    public FpoversiktRestKlient() {
        this.restKlient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpoint = UriBuilder.fromUri(restConfig.endpoint()).path(ANNEN_PART_UTTAKSPLAN_PATH).build();
    }

    @Override
    public void lagreAnnenPartsUttaksplan(FpoversiktUttaksplanRequest request) {
        restKlient.sendReturnOptional(RestRequest.newPOSTJson(request, endpoint, restConfig), String.class);
    }
}
