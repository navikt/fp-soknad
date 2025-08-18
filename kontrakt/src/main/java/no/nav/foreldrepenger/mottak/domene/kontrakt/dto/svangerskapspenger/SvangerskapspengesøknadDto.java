package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.svangerskapspenger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.AnnenInntektDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.FrilansDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.NæringDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.SøknadDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.VedleggDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.validering.VedlegglistestørrelseConstraint;

public record SvangerskapspengesøknadDto(LocalDate mottattdato,
                                         @NotNull @Valid BarnSvpDto barn,
                                         @Valid BrukerRolle rolle,
                                         @NotNull @Valid Målform språkkode,
                                         @Valid FrilansDto frilans,
                                         @Valid NæringDto egenNæring,
                                         @Valid @Size(max = 20) List<@Valid @NotNull AnnenInntektDto> andreInntekterSiste10Mnd,
                                         @Valid @Size(max = 20) List<@Valid @NotNull UtenlandsoppholdsperiodeDto> utenlandsopphold,
                                         @NotNull @Valid @Size(min = 1, max = 20) List<@Valid @NotNull TilretteleggingbehovDto> tilretteleggingsbehov,
                                         @NotNull @Valid @Size(max = 20) List<@Valid @NotNull AvtaltFerieDto> avtaltFerie,
                                         @NotNull @Valid @VedlegglistestørrelseConstraint @Size(min = 1,
                                             max = 20) List<@Valid @NotNull VedleggDto> vedlegg) implements SøknadDto {

    public SvangerskapspengesøknadDto {
        utenlandsopphold = Optional.ofNullable(utenlandsopphold).orElse(List.of());
        tilretteleggingsbehov = Optional.ofNullable(tilretteleggingsbehov).orElse(List.of());
        andreInntekterSiste10Mnd = Optional.ofNullable(andreInntekterSiste10Mnd).orElse(List.of());
        avtaltFerie = Optional.ofNullable(avtaltFerie).orElse(List.of());
        vedlegg = Optional.ofNullable(vedlegg).orElse(List.of());
    }
}
