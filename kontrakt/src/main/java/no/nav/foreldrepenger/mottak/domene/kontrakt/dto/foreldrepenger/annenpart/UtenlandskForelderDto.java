package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.annenpart;

import com.neovisionaries.i18n.CountryCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;

import static no.nav.foreldrepenger.common.domain.validation.InputValideringRegex.FRITEKST;

public record UtenlandskForelderDto(@NotNull @Valid Fødselsnummer fnr,
                                    @NotNull @Pattern(regexp = FRITEKST) String fornavn,
                                    @NotNull @Pattern(regexp = FRITEKST) String etternavn,
                                    @NotNull CountryCode bostedsland,
                                    @NotNull @Valid Rettigheter rettigheter) implements AnnenForelderDto {
}
