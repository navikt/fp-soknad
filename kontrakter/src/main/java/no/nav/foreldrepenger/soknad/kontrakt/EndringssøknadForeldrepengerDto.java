package no.nav.foreldrepenger.soknad.kontrakt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.barn.BarnDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanMapper;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.validering.VedlegglistestørrelseConstraint;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.VedleggDto;

public record EndringssøknadForeldrepengerDto(LocalDateTime mottattdato,
                                              @Valid @NotNull Saksnummer saksnummer,
                                              @Valid @NotNull SøkerDto søkerinfo,
                                              @Valid BrukerRolle rolle,
                                              @Valid Målform språkkode,
                                              @Valid @NotNull BarnDto barn,
                                              @Valid AnnenForelderDto annenForelder,
                                              @Valid @NotNull UttaksplanDto uttaksplan,
                                              @Valid FellesUttaksplanDto fellesUttaksplan,
                                              @VedlegglistestørrelseConstraint @Size(max = 100) List<@Valid @NotNull VedleggDto> vedlegg) implements SøknadDto {

    public EndringssøknadForeldrepengerDto {
        if (fellesUttaksplan != null) {
            uttaksplan = FellesUttaksplanMapper.tilUttaksplanForEndringssøknad(fellesUttaksplan, uttaksplan);
        }
        vedlegg = Optional.ofNullable(vedlegg).map(ArrayList::new).orElse(new ArrayList<>());
    }

    public EndringssøknadForeldrepengerDto(LocalDateTime mottattdato,
                                           Saksnummer saksnummer,
                                           SøkerDto søkerinfo,
                                           BrukerRolle rolle,
                                           Målform språkkode,
                                           BarnDto barn,
                                           AnnenForelderDto annenForelder,
                                           UttaksplanDto uttaksplan,
                                           List<VedleggDto> vedlegg) {
        this(mottattdato, saksnummer, søkerinfo, rolle, språkkode, barn, annenForelder, uttaksplan, null, vedlegg);
    }
}
