package no.nav.foreldrepenger.soknad.innsending.fordel.pdf.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "fpdokgen.base.url", endpointDefault = "http://fp-dokgen", scopesProperty = "fpdokgen.scopes", scopesDefault = "api://prod-gcp.teamforeldrepenger.fp-dokgen/.default")
public class FpDokgenRestKlient {

    private static final Logger LOG = LoggerFactory.getLogger(FpDokgenRestKlient.class);

    protected static final String API_PATH = "/api";
    private static final String V1_GENERER_PDF_PATH = "/v1/dokument/generer/pdf";
    protected static final String APPLICATION_PDF = "application/pdf";

    private final RestClient restClient;
    private final RestConfig restConfig;

    public FpDokgenRestKlient() {
        this(RestClient.client());
    }

    public FpDokgenRestKlient(RestClient restClient) {
        this.restClient = restClient;
        this.restConfig = RestConfig.forClient(FpDokgenRestKlient.class);
    }

    public byte[] genererPdf(FpDokgenRequest requestDto) {
        LOG.info("Genererer PDF for dokument: {}", requestDto.malNavn());
        var endpoint = UriBuilder.fromUri(restConfig.endpoint()).path(API_PATH).path(V1_GENERER_PDF_PATH).build();
        var request = RestRequest.newPOSTJson(requestDto, endpoint, restConfig).header(HttpHeaders.ACCEPT, APPLICATION_PDF);
        return restClient.sendReturnByteArray(request);
    }
}
