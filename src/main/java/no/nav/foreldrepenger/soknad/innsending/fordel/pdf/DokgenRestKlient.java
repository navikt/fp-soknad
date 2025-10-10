package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.soknad.innsending.UtalelseOmTilbakebetaling;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
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
            case ForeldrepengesøknadDto ignored -> "søknad-foreldrepenger";
            case EndringssøknadForeldrepengerDto ignored1 -> "søknad-foreldrepenger-endring";
            case SvangerskapspengesøknadDto ignored -> "søknad-svangerskapspenger";
            case EngangsstønadDto ignored -> "søknad-engangsstønad";
        };
        var språk = "nb"; // Hardkodet, men kan bruke søknadDto.getSpråk().toLowerCase() for å støtte flere språk
        return String.format("/template/%s/template_%s", templateNavn, språk);
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
