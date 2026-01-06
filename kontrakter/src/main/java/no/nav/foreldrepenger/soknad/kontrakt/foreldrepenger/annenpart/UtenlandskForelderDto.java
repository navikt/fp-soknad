package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart;

import static no.nav.foreldrepenger.kontrakter.felles.validering.InputValideringRegex.FRITEKST;

import com.neovisionaries.i18n.CountryCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;


public record UtenlandskForelderDto(@NotNull @Valid Fødselsnummer fnr,
                                    @NotNull @Pattern(regexp = FRITEKST) String fornavn,
                                    @NotNull @Pattern(regexp = FRITEKST) String etternavn,
                                    @NotNull CountryCode bostedsland,
                                    @NotNull @Valid Rettigheter rettigheter) implements AnnenForelderDto {
}
