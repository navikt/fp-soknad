package no.nav.foreldrepenger.soknad.kontrakt;

import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.soknad.kontrakt.barn.BarnDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.VedleggDto;

public sealed interface SøknadDto permits EndringssøknadForeldrepengerDto, ForeldrepengesøknadDto, EngangsstønadDto, SvangerskapspengesøknadDto {
    LocalDateTime mottattdato();
    Målform språkkode();
    SøkerDto søkerinfo();
    BarnDto barn();
    List<VedleggDto> vedlegg();
}
