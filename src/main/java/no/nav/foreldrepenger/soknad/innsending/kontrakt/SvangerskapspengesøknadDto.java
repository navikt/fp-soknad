package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.AvtaltFerieDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.BarnSvpDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.TilretteleggingbehovDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.validering.VedlegglistestørrelseConstraint;

public record SvangerskapspengesøknadDto(LocalDateTime mottattdato,
                                         @Valid @NotNull SøkerDto søkerinfo,
                                         @NotNull @Valid Målform språkkode,
                                         @NotNull @Valid @JsonProperty("barn") BarnSvpDto barnSvp, // Litt hack eller?
                                         @Valid FrilansDto frilans,
                                         @Valid NæringDto egenNæring,
                                         @Valid @Size(max = 20) List<@Valid @NotNull AnnenInntektDto> andreInntekterSiste10Mnd,
                                         @Valid @Size(max = 20) List<@Valid @NotNull UtenlandsoppholdsperiodeDto> utenlandsopphold,
                                         @NotNull @Valid @Size(min = 1, max = 20) List<@Valid @NotNull TilretteleggingbehovDto> tilretteleggingsbehov,
                                         @NotNull @Valid @Size(max = 20) List<@Valid @NotNull AvtaltFerieDto> avtaltFerie,
                                         @NotNull @Valid @VedlegglistestørrelseConstraint @Size(min = 1, max = 20) List<@Valid @NotNull VedleggDto> vedlegg) implements SøknadDto {

    public SvangerskapspengesøknadDto {
        andreInntekterSiste10Mnd = Optional.ofNullable(andreInntekterSiste10Mnd).orElse(List.of());
        utenlandsopphold = Optional.ofNullable(utenlandsopphold).orElse(List.of());
        tilretteleggingsbehov = Optional.ofNullable(tilretteleggingsbehov).orElse(List.of());
        avtaltFerie = Optional.ofNullable(avtaltFerie).orElse(List.of());
        vedlegg = Optional.ofNullable(vedlegg).orElse(List.of());
    }

    @Override
    public BarnDto barn() {
        var barnet = barnSvp();
        if (barnet.fødselsdato() != null) {
            return new FødselDto(1, barnet.fødselsdato(), barnet.termindato());
        }
        return new TerminDto(1, barnet.termindato(), null);
    }
}
