package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.endringssøknad;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.domain.Saksnummer;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.BarnDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.VedleggDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.uttaksplan.UttaksplanDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.foreldrepenger.common.domain.validation.InputValideringRegex.FRITEKST;


public record EndringssøknadForeldrepengerDto(LocalDate mottattdato,
                                              @Valid BrukerRolle rolle,
                                              @Valid Målform språkkode,
                                              @Valid @NotNull BarnDto barn,
                                              @Valid AnnenForelderDto annenForelder,
                                              @Pattern(regexp = FRITEKST) String tilleggsopplysninger,
                                              @Valid @NotNull UttaksplanDto uttaksplan,
                                              @Valid @NotNull Saksnummer saksnummer,
                                              @Valid @Size(max = 100) List<@Valid VedleggDto> vedlegg) implements EndringssøknadDto {

    public EndringssøknadForeldrepengerDto {
        vedlegg = Optional.ofNullable(vedlegg).map(ArrayList::new).orElse(new ArrayList<>());
    }
}
