package no.nav.foreldrepenger.mottak.domene.vedlegg.sjekkere.virusscan;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.mottak.domene.vedlegg.error.AttachmentVirusException;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static no.nav.foreldrepenger.mottak.domene.vedlegg.sjekkere.virusscan.Result.OK;


// TODO: Denne må nokk fikses/endres på! Er ikke støtte for Mulitpart eller Put i felles.
//  https://docs.nais.io/services/antivirus/?h=clamav#getting-started

@ApplicationScoped
@RestClientConfig(
    tokenConfig = TokenFlow.NO_AUTH_NEEDED,
    endpointProperty = "clamav.base.url",
    //endpointDefault = "http://clamav.nais-system.svc.nais.local"
    endpointDefault = "http://clamav.nais-system"
)
public class VirusScanKlient {
    private static final Logger LOG = LoggerFactory.getLogger(VirusScanKlient.class);
    private static final String SCAN_PATH = "/scan";

    private final HttpClient httpClient;
    private final RestConfig restConfig;

    public VirusScanKlient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .proxy(HttpClient.Builder.NO_PROXY)
            .build();
        this.restConfig = RestConfig.forClient(VirusScanKlient.class);
    }

    public void scan(byte[] bytes, UUID uuid) {
        if (bytes == null) {
            return;
        }
        var request = HttpRequest.newBuilder()
            .uri(UriBuilder.fromUri(restConfig.endpoint()).path(SCAN_PATH).build())
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
            .PUT(HttpRequest.BodyPublishers.ofByteArray(bytes))
            .build();
        var scanResults = sendAndHandle(request);
        if (scanResults == null || scanResults.size() != 1) {
            LOG.warn("Uventet respons med lengde {}, forventet lengde er 1", scanResults.size());
            throw new AttachmentVirusException(uuid.toString());
        }
        var scanResult = scanResults.getFirst();
        LOG.trace("Fikk scan result {}", scanResult);
        if (OK.equals(scanResult.getResult())) {
            LOG.trace("Ingen virus i {}", uuid);
            return;
        }
        LOG.warn("Fant virus!, status {}", scanResult.getResult());
        throw new AttachmentVirusException("Virus påvist i dokument med id " + uuid);
    }

    private List<ScanResult> sendAndHandle(HttpRequest request) {
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == HttpURLConnection.HTTP_NO_CONTENT) {
                return null;
            }
            if ((status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE)) {
                return List.of(DefaultJsonMapper.fromJson(response.body(), ScanResult[].class));
            }
            if (status == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new ManglerTilgangException("F-468816", "Feilet mot clamav");
            }
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra clamav", status), status);
        } catch (IOException e) {
            throw new IntegrasjonException("F-157391", "Uventet IO-exception mot endepunkt", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegrasjonException("F-157392", "InterruptedException ved kall mot endepunkt", e);
        }
    }
}
