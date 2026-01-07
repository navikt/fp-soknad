package no.nav.foreldrepenger.soknad.kontrakt.builder;

import no.nav.foreldrepenger.soknad.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;

public interface SøknadBuilder<S> {
    SøknadBuilder<S> medSøkerinfo(SøkerDto søkerinfo);
    SøknadDto build();
}
