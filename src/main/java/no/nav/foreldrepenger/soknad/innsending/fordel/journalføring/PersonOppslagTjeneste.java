package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PersonOppslagTjeneste {

    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES);
    private static final LRUCache<String, Fødselsnummer> AKTØR_FNR = new LRUCache<>(2000, CACHE_ELEMENT_LIVE_TIME_MS);
    private static final LRUCache<String, AktørId> FNR_AKTØR = new LRUCache<>(2000, CACHE_ELEMENT_LIVE_TIME_MS);

    public AktørId hentAkøridFor(String fnr) {
        return hentAkøridFor(new Fødselsnummer(fnr));
    }

    public AktørId hentAkøridFor(Fødselsnummer fnr) {
        return Optional.ofNullable(FNR_AKTØR.get(fnr.value()))
            .orElseGet(() -> {
                var aktørId = new AktørId("99" + fnr.value()); // TODO: Fpoversikt eller direkte integrasjon?
                FNR_AKTØR.put(fnr.value(), aktørId);
                AKTØR_FNR.put(aktørId.value(), fnr);
                return aktørId;
            });
    }
}
