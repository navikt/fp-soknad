package no.nav.foreldrepenger.soknad.innsending;

import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.EttersendelseDto;

public interface InnsendingTjeneste {
    void lagreSøknadInnsending(SøknadDto søknad);

    void lagreEttersendelseInnsending(EttersendelseDto ettersendelse);

    void lagreUttalelseOmTilbakekreving(EttersendelseDto ettersendelse);
}
