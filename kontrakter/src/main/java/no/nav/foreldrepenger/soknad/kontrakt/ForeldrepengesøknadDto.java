package no.nav.foreldrepenger.soknad.kontrakt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.soknad.kontrakt.barn.BarnDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanMapper;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.AnnenInntektDto;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.FrilansDto;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.NæringDto;
import no.nav.foreldrepenger.soknad.kontrakt.validering.VedlegglistestørrelseConstraint;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.VedleggDto;

public record ForeldrepengesøknadDto(LocalDateTime mottattdato,
                                     @Valid @NotNull SøkerDto søkerinfo,
                                     @Valid BrukerRolle rolle,
                                     @Valid Målform språkkode,
                                     @Valid @NotNull BarnDto barn,
                                     @Valid FrilansDto frilans,
                                     @Valid NæringDto egenNæring,
                                     @Size(max = 40) List<@Valid @NotNull AnnenInntektDto> andreInntekterSiste10Mnd,
                                     @Valid AnnenForelderDto annenForelder,
                                     @Valid @NotNull Dekningsgrad dekningsgrad,
                                     @Valid @NotNull UttaksplanDto uttaksplan,
                                     @Valid FellesUttaksplanDto fellesUttaksplan,
                                     @Size(max = 40) List<@Valid @NotNull UtenlandsoppholdsperiodeDto> utenlandsopphold,
                                     @VedlegglistestørrelseConstraint @Size(max = 100)  List<@Valid @NotNull VedleggDto> vedlegg) implements SøknadDto {
    public ForeldrepengesøknadDto {
        if (fellesUttaksplan != null) {
            uttaksplan = FellesUttaksplanMapper.tilUttaksplan(fellesUttaksplan, uttaksplan);
        }
        andreInntekterSiste10Mnd = Optional.ofNullable(andreInntekterSiste10Mnd).orElse(List.of());
        utenlandsopphold = Optional.ofNullable(utenlandsopphold).orElse(List.of());
        vedlegg = Optional.ofNullable(vedlegg).orElse(List.of());
    }

    public ForeldrepengesøknadDto(LocalDateTime mottattdato,
                                  SøkerDto søkerinfo,
                                  BrukerRolle rolle,
                                  Målform språkkode,
                                  BarnDto barn,
                                  FrilansDto frilans,
                                  NæringDto egenNæring,
                                  List<AnnenInntektDto> andreInntekterSiste10Mnd,
                                  AnnenForelderDto annenForelder,
                                  Dekningsgrad dekningsgrad,
                                  UttaksplanDto uttaksplan,
                                  List<UtenlandsoppholdsperiodeDto> utenlandsopphold,
                                  List<VedleggDto> vedlegg) {
        this(mottattdato, søkerinfo, rolle, språkkode, barn, frilans, egenNæring, andreInntekterSiste10Mnd, annenForelder,
            dekningsgrad, uttaksplan, null, utenlandsopphold, vedlegg);
    }
}
