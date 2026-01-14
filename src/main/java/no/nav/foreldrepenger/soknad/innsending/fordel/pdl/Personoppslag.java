package no.nav.foreldrepenger.soknad.innsending.fordel.pdl;

import no.nav.foreldrepenger.kontrakter.felles.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;

import java.util.Optional;

public interface Personoppslag {

    AktørId aktørId(String fnr);
    AktørId aktørId(Fødselsnummer fnr);
    Optional<AktørId> finnAktørId(Fødselsnummer fnr);
}
