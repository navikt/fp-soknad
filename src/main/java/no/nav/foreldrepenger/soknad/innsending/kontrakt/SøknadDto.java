package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.time.LocalDateTime;
import java.util.List;

public sealed interface SøknadDto permits EndringssøknadForeldrepengerDto, ForeldrepengesøknadDto, EngangsstønadDto, SvangerskapspengesøknadDto {
    LocalDateTime mottattdato();
    SøkerDto søkerinfo();
    BarnDto barn();
    List<VedleggDto> vedlegg();
}
