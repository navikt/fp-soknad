package no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse;


import no.nav.foreldrepenger.soknad.innsending.kontrakt.Saksnummer;

public record TilbakebetalingUttalelseDto(YtelseType type, Saksnummer saksnummer, String dialogId, BrukerTekstDto brukerTekst) {
}
