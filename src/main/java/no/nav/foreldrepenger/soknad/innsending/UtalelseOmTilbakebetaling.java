package no.nav.foreldrepenger.soknad.innsending;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.kontrakter.fpsoknad.ettersendelse.BrukerTekstDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.ettersendelse.YtelseType;

public record UtalelseOmTilbakebetaling(@NotNull YtelseType type, @Valid @NotNull BrukerTekstDto brukertekst) {
}
