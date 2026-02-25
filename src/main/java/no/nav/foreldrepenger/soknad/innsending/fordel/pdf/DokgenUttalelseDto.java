package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import java.time.LocalDateTime;

record DokgenUttalelseDto(LocalDateTime innsendtDato, String saksnummer, String fnr, String ytelse, String tilsvar) {
}
