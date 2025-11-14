package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.kontrakter.fpsoknad.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.EngangsstønadDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.SøknadDto;
import no.nav.foreldrepenger.soknad.innsending.UtalelseOmTilbakebetaling;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;


@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.NO_AUTH_NEEDED, endpointProperty = "dokgen.rest.base.url", endpointDefault = "http://fpdokgen")
public class DokgenRestKlient {

    private static final Logger LOG = LoggerFactory.getLogger(DokgenRestKlient.class);
    private static final Environment ENV = Environment.current();
    private final RestClient restClient;
    private final RestConfig restConfig;


    public DokgenRestKlient() {
        this(RestClient.client());
    }

    public DokgenRestKlient(RestClient restClient) {
        this.restClient = restClient;
        this.restConfig = RestConfig.forClient(DokgenRestKlient.class);
    }

    public byte[] genererPdf(ForsendelseEntitet metadata, SøknadDto søknad) throws TekniskException {
        var templatePath = utledTemplatePath(søknad);
        var dokgenDto = new DokgenSøknadDto(metadata.getForsendelseMottatt(), søknad);
        var endpoint = UriBuilder.fromUri(restConfig.endpoint()).path(templatePath).path("/create-pdf-variation").build();
        var request = RestRequest.newPOSTJson(dokgenDto, endpoint, restConfig);
        LOG.info("Path {}", endpoint);
        return restClient.sendReturnByteArray(request);
    }

    private String utledTemplatePath(SøknadDto søknadDto) {
        var templateNavn = switch (søknadDto) {
            case ForeldrepengesøknadDto _ -> "søknad-foreldrepenger";
            case EndringssøknadForeldrepengerDto _ -> "søknad-foreldrepenger-endring";
            case SvangerskapspengesøknadDto _ -> "søknad-svangerskapspenger";
            case EngangsstønadDto _ -> "søknad-engangsstønad";
        };
        var språkvalg = switch (søknadDto.språkkode()) {
            case NB -> "nb";
            case NN -> "nn";
            case EN, E -> "en";
        };
        return String.format("/template/%s/template_%s", templateNavn, språkvalg);
    }

    public byte[] genererUttalelseOmTilbakekrevingPDF(ForsendelseEntitet metadata, UtalelseOmTilbakebetaling svar) {
        var body = new UttalelseDtoDokgen(
            metadata.getForsendelseMottatt(),
            metadata.getSaksnummer().orElseThrow(),
            metadata.getBrukersFnr(),
            svar.type().name().toLowerCase(),
            svar.brukertekst().tekst()
        );
        var endpoint = UriBuilder.fromUri(restConfig.endpoint()).path("/template/selvbetjening-tilsvar-tilbakebetalingvarsel/template_nb/create-pdf-variation").build();
        var request = RestRequest.newPOSTJson(body, endpoint, restConfig);
        return restClient.sendReturnByteArray(request);
    }

    record UttalelseDtoDokgen(LocalDateTime innsendtDato, String saksnummer, String fnr, String ytelse, String tilsvar) {
    }

    record DokgenSøknadDto(LocalDateTime mottattdato,  @JsonUnwrapped SøknadDto søknad) {
    }

}
