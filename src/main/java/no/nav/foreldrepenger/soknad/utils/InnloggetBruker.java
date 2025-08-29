package no.nav.foreldrepenger.soknad.utils;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@ApplicationScoped
public class InnloggetBruker {

    public String brukerFraKontekst() {
        return KontekstHolder.getKontekst().getUid();
    }
}
