package no.nav.foreldrepenger.soknad.innsending.fordel.pdl;

import no.nav.foreldrepenger.soknad.innsending.kontrakt.AktørId;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Fødselsnummer;

public interface Personoppslag {

    AktørId aktørId(String fnr);
    AktørId aktørId(Fødselsnummer fnr);
}
