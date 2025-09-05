package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.util.List;

import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;

public sealed interface SøknadDto permits EndringssøknadForeldrepengerDto, ForeldrepengesøknadDto, EngangsstønadDto, SvangerskapspengesøknadDto {

    BarnDto barn();
    List<VedleggDto> vedlegg();
}
