package no.nav.foreldrepenger.mottak.domene.utils;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@ApplicationScoped
public class InnloggetBruker {

    public String brukerFraKontekst() {
        return KontekstHolder.getKontekst().getUid();
    }
}
