package no.nav.foreldrepenger.soknad.innsending.fordel.pdl;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.Dependent;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AktørId;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Fødselsnummer;
import no.nav.vedtak.felles.integrasjon.person.AbstractPersonKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.util.LRUCache;

@RestClientConfig(
    tokenConfig = TokenFlow.AZUREAD_CC,
    endpointProperty = "pdl.base.url",
    endpointDefault = "https://pdl-api.prod-fss-pub.nais.io/graphql",
    scopesProperty = "pdl.scopes",
    scopesDefault = "api://prod-fss.pdl.pdl-api/.default")
@Dependent
public class PdlKlientSystem extends AbstractPersonKlient implements Personoppslag {

    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES);
    private static final LRUCache<String, AktørId> FNR_AKTØR = new LRUCache<>(2000, CACHE_ELEMENT_LIVE_TIME_MS);


    @Override
    public AktørId aktørId(String fnr) {
        return aktørId(new Fødselsnummer(fnr));
    }

    @Override
    public AktørId aktørId(Fødselsnummer fnr) {
        return Optional.ofNullable(FNR_AKTØR.get(fnr.value())).orElseGet(() -> {
            var aktørId = new AktørId(hentAktørIdForPersonIdent(fnr.value()).orElseThrow());
            FNR_AKTØR.put(fnr.value(), aktørId);
            return aktørId;
        });
    }
}
