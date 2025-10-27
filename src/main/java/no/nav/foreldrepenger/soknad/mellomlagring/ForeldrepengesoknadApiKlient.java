package no.nav.foreldrepenger.soknad.mellomlagring;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(
    tokenConfig = TokenFlow.ADAPTIVE,
    endpointProperty = "api.base.url",
    endpointDefault = "http://foreldrepengesoknad-api",
    scopesProperty = "api.scopes",
    scopesDefault = "api://prod-gcp.teamforeldrepenger.foreldrepengesoknad-api/.default"
)
public class ForeldrepengesoknadApiKlient {

    private static final Logger LOG = LoggerFactory.getLogger(ForeldrepengesoknadApiKlient.class);
    private static final String MELLOMLAGRING = "/rest/storage";

    private final URI mellomlagringUrl;

    private final RestClient restKlient;
    private final RestConfig restConfig;

    public ForeldrepengesoknadApiKlient() {
        this.restKlient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        var endpoint = restConfig.fpContextPath();
        this.mellomlagringUrl = lagURI(endpoint, MELLOMLAGRING);
    }

    public Optional<String> hentMellomlagretSoknad(YtelseMellomlagringType ytelseMellomlagringType) {
        var url = UriBuilder.fromUri(mellomlagringUrl)
            .path("/" + ytelseMellomlagringType.name())
            .build();
        var request = RestRequest.newGET(url, restConfig);
        LOG.info("Henter mellomlagret søknad fra API");
        var respons = restKlient.sendReturnOptional(request, String.class);
        if (respons.isPresent()) {
            LOG.info("Mellomlagret søknad hentet fra API");
        }
        return respons;
    }

    public Optional<byte[]> hentMellomlagretVedlegg(YtelseMellomlagringType ytelseMellomlagringType, String key) {
        var url = UriBuilder.fromUri(mellomlagringUrl)
            .path("/" + ytelseMellomlagringType.name())
            .path("/vedlegg/" + key)
            .build();
        var request = RestRequest.newGET(url, restConfig);
        LOG.info("Henter mellomlagret vedlegg fra API");
        var respons = restKlient.sendReturnOptional(request, byte[].class);
        if (respons.isPresent()) {
            LOG.info("Mellomlagret vedlegg hentet fra API");
        }
        return respons;
    }


    private URI lagURI(URI context, String api) {
        return UriBuilder.fromUri(context).path(api).build();
    }
}
