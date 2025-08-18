package no.nav.foreldrepenger.mottak.domene.kontrakt.dto;

import java.util.List;

import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.engangsstønad.EngangsstønadDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.svangerskapspenger.SvangerskapspengesøknadDto;

public interface SøknadDto extends Innsending {
    BrukerRolle rolle();

    Målform språkkode();

    List<UtenlandsoppholdsperiodeDto> utenlandsopphold();

    List<VedleggDto> vedlegg();

    default String navn() {
        if (this instanceof ForeldrepengesøknadDto) {
            return "foreldrepenger";
        }
        if (this instanceof EngangsstønadDto) {
            return "engangsstønad";
        }
        if (this instanceof SvangerskapspengesøknadDto) {
            return "svangersskapspenger";
        }
        throw new IllegalStateException("Utvikerfeil: Kan ikke ha en annen ytelse enn fp, svp eller es!");
    }
}
