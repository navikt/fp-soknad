package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.engangsstønad;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.BarnDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.SøknadDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.VedleggDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record EngangsstønadDto(LocalDate mottattdato,
                               @NotNull Målform språkkode,
                               @Valid BrukerRolle rolle,
                               @Valid @NotNull BarnDto barn,
                               @Valid @Size(max = 40) List<@Valid @NotNull UtenlandsoppholdsperiodeDto> utenlandsopphold,
                               @Valid @Size(max = 100) List<@Valid @NotNull VedleggDto> vedlegg) implements SøknadDto {

    public EngangsstønadDto {
        vedlegg = Optional.ofNullable(vedlegg).orElse(List.of());
    }
}
