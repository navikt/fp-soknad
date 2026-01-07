package no.nav.foreldrepenger.soknad.kontrakt.ettersendelse;

import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;

public record TilbakebetalingUttalelseDto(YtelseType type, Saksnummer saksnummer, String dialogId, BrukerTekstDto brukerTekst) {
}
