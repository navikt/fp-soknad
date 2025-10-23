package no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Fødselsnummer;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Saksnummer;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;

public record EttersendelseDto(LocalDateTime mottattdato,
                               @NotNull @Valid Saksnummer saksnummer,
                               @NotNull @Valid Fødselsnummer fnr,
                               @NotNull YtelseType type,
                               @Valid BrukerTekstDto brukerTekst,
                               @Valid @NotNull @Size(max = 40) List<@Valid VedleggDto> vedlegg) {

    public boolean erInnsendingAvUttalelseOmTilbakekreving() {
        return vedlegg().isEmpty() && brukerTekst() != null;
    }

    public EttersendelseDto {
        vedlegg = Optional.ofNullable(vedlegg).map(ArrayList::new).orElse(new ArrayList<>());
    }
}
