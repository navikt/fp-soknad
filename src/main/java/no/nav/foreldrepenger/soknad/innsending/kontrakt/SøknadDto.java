package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.common.domain.BrukerRolle;

public sealed interface SøknadDto permits EndringssøknadForeldrepengerDto, ForeldrepengesøknadDto, EngangsstønadDto, SvangerskapspengesøknadDto {
    LocalDateTime mottattdato();
    BrukerRolle rolle();
    SøkerDto søkerinfo();
    BarnDto barn();
    List<VedleggDto> vedlegg();
}
