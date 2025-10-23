package no.nav.foreldrepenger.soknad.innsending.fordel.pdl;

import no.nav.foreldrepenger.kontrakter.fpsoknad.Fødselsnummer;
import no.nav.foreldrepenger.soknad.innsending.fordel.AktørId;

public interface Personoppslag {

    AktørId aktørId(String fnr);
    AktørId aktørId(Fødselsnummer fnr);
}
