package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.AnnenInntektDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.BarnDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.FrilansDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.NæringDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.SøknadDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.VedleggDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.uttaksplan.UttaksplanDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static no.nav.foreldrepenger.common.domain.validation.InputValideringRegex.FRITEKST;

public record ForeldrepengesøknadDto(LocalDate mottattdato,
                                     @Valid BrukerRolle rolle,
                                     @Valid Målform språkkode,
                                     @Valid FrilansDto frilansInformasjon,
                                     @Valid NæringDto selvstendigNæringsdrivendeInformasjon,
                                     @Valid @Size(max = 40) List<@Valid @NotNull AnnenInntektDto> andreInntekterSiste10Mnd,
                                     @Valid @NotNull BarnDto barn,
                                     @Valid AnnenForelderDto annenForelder,
                                     @Valid @NotNull Dekningsgrad dekningsgrad,
                                     @Valid @NotNull UttaksplanDto uttaksplan,
                                     @Pattern(regexp = FRITEKST) String tilleggsopplysninger,
                                     @Valid @Size(max = 40) List<@Valid @NotNull UtenlandsoppholdsperiodeDto> utenlandsopphold,
                                     @Valid @Size(max = 100) List<@Valid VedleggDto> vedlegg) implements SøknadDto {
    public ForeldrepengesøknadDto {
        andreInntekterSiste10Mnd = Optional.ofNullable(andreInntekterSiste10Mnd).orElse(List.of());
        vedlegg = Optional.ofNullable(vedlegg).orElse(List.of());
    }


}
