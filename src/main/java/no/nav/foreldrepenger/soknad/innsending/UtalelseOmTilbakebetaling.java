package no.nav.foreldrepenger.soknad.innsending;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.BrukerTekstDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.YtelseType;

public record UtalelseOmTilbakebetaling(@NotNull YtelseType type, @Valid @NotNull BrukerTekstDto brukertekst) {
}
