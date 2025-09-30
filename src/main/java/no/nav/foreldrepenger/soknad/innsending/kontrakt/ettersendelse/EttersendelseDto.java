package no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.domain.Saksnummer;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;

public record EttersendelseDto(LocalDate mottattdato,
                               @NotNull YtelseType type,
                               @NotNull @Valid Saksnummer saksnummer,
                               @Valid BrukerTekstDto brukerTekst,
                               @Valid @NotNull @Size(max = 40) List<@Valid VedleggDto> vedlegg) {

    public EttersendelseDto {
        vedlegg = Optional.ofNullable(vedlegg).map(ArrayList::new).orElse(new ArrayList<>());
    }
}
