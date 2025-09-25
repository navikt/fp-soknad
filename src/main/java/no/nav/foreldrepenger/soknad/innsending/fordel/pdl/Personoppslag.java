package no.nav.foreldrepenger.soknad.innsending.fordel.pdl;

import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;

public interface Personoppslag {

    AktørId aktørId(String fnr);
    AktørId aktørId(Fødselsnummer fnr);
}
