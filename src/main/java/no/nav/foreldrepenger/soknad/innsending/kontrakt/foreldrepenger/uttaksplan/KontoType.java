package no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Kodeverdi;

public enum KontoType implements Kodeverdi {
    FELLESPERIODE,
    MØDREKVOTE,
    FEDREKVOTE,
    FORELDREPENGER,
    FORELDREPENGER_FØR_FØDSEL;

    @Override
    public String getKode() {
        return name();
    }
}
