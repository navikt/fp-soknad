package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
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
@RestClientConfig(tokenConfig = TokenFlow.NO_AUTH_NEEDED, endpointProperty = "dokgen.rest.base.url", endpointDefault = "http://fpdokgen.teamforeldrepenger")
public class DokgenRestKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;


    public DokgenRestKlient() {
        this(RestClient.client());
    }

    public DokgenRestKlient(RestClient restClient) {
        this.restClient = restClient;
        this.restConfig = RestConfig.forClient(DokgenRestKlient.class);
    }

    public byte[] genererPdf(SøknadDto søknad) throws TekniskException {
        var templatePath = utledTemplatePath(søknad);
        var endpoint = UriBuilder.fromUri(restConfig.endpoint()).path(templatePath).path("/create-pdf-variation").build();
        var request = RestRequest.newPOSTJson(søknad, endpoint, restConfig);
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
}
