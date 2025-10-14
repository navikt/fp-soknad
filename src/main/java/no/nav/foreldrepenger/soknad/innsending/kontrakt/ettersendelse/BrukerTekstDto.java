package no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse;

import static no.nav.foreldrepenger.common.domain.validation.InputValideringRegex.FRITEKST;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;

public record BrukerTekstDto(@NotNull DokumentTypeId dokumentType, @NotNull @Pattern(regexp = FRITEKST) String tekst) {
}
