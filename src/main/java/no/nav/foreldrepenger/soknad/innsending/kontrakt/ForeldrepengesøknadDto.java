package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.validering.VedlegglistestørrelseConstraint;

public record ForeldrepengesøknadDto(LocalDate mottattdato,
                                     @Valid BrukerRolle rolle,
                                     @Valid Målform språkkode,
                                     @Valid FrilansDto frilans,
                                     @Valid NæringDto egenNæring,
                                     @Valid @Size(max = 40) List<@Valid @NotNull AnnenInntektDto> andreInntekterSiste10Mnd,
                                     @Valid @NotNull BarnDto barn,
                                     @Valid AnnenForelderDto annenForelder,
                                     @Valid @NotNull Dekningsgrad dekningsgrad,
                                     @Valid @NotNull UttaksplanDto uttaksplan,
                                     @Valid @Size(max = 40) List<@Valid @NotNull UtenlandsoppholdsperiodeDto> utenlandsopphold,
                                     @Valid @VedlegglistestørrelseConstraint @Size(max = 100) List<@Valid VedleggDto> vedlegg) implements SøknadDto {
    public ForeldrepengesøknadDto {
        andreInntekterSiste10Mnd = Optional.ofNullable(andreInntekterSiste10Mnd).orElse(List.of());
        vedlegg = Optional.ofNullable(vedlegg).orElse(List.of());
    }


}
