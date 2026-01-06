package no.nav.foreldrepenger.soknad.kontrakt.ettersendelse;

import static no.nav.foreldrepenger.kontrakter.felles.validering.InputValideringRegex.FRITEKST;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;

public record BrukerTekstDto(@NotNull DokumentTypeId dokumentType, @NotNull @Pattern(regexp = FRITEKST) String tekst) {
}
