package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;

import java.util.Optional;

@ApplicationScoped
public class PersonInformasjon {

    public AktørId hentAkøridFor(String personIdent) {
        return null; // TODO: Fpoversikt eller direkte integrasjon?
    }

    public Optional<String> hentPersonIdentForAktørId(String avsenderAktørId) {
        return null; // TODO: Fpoversikt eller direkte integrasjon?
    }

    public String hentNavn(BehandlingTema behandlingstema, String avsenderAktørId) {
        return null; // TODO: Fpoversikt eller direkte integrasjon?
    }
}
