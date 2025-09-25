package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, application = FpApplication.FPOVERSIKT)
public class FpoversiktRestKlient {

    private static final String ANNENPART_AKTØRID_PATH = "/api/annenPart/aktorid";
    private static final String OPPSLAG_SØKER_PATH = "/api/person/info-med-arbeidsforhold";

    private final RestClient restKlient;
    private final RestConfig restConfig;
    private final URI annenpartAktøridEndpoint;
    private final URI oppslagSøkerEndpoint;

    public FpoversiktRestKlient() {
        this.restKlient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.annenpartAktøridEndpoint = UriBuilder.fromUri(restConfig.fpContextPath()).path(ANNENPART_AKTØRID_PATH).build();
        this.oppslagSøkerEndpoint = UriBuilder.fromUri(restConfig.fpContextPath()).path(OPPSLAG_SØKER_PATH).build();
    }

    public AktørId aktørIdFraFnr(Fødselsnummer annenpartsFnr) {
        var dto = new AnnenPartRequest(annenpartsFnr);
        var request = RestRequest.newPOSTJson(dto, annenpartAktøridEndpoint, restConfig);
        return restKlient.send(request, AktørId.class);
    }

    public PersonMedArbeidsforholdDto hentSøkerinfo(Fødselsnummer søkerFnr) {
        var request = RestRequest.newGET(oppslagSøkerEndpoint, restConfig);
        return restKlient.send(request, PersonMedArbeidsforholdDto.class);
    }



    private record AnnenPartRequest(Fødselsnummer fnr) {}
}
