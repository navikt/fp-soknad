package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.time.LocalDate;
import java.util.List;

public sealed interface SøknadDto permits EndringssøknadForeldrepengerDto, ForeldrepengesøknadDto, EngangsstønadDto, SvangerskapspengesøknadDto {
    LocalDate mottattdato();
    BarnDto barn();
    List<VedleggDto> vedlegg();
}
