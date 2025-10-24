package no.nav.foreldrepenger.soknad.innsending;

import no.nav.foreldrepenger.kontrakter.fpsoknad.SøknadDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.ettersendelse.EttersendelseDto;

public interface InnsendingTjeneste {
    void lagreSøknadInnsending(SøknadDto søknad);

    void lagreEttersendelseInnsending(EttersendelseDto ettersendelse);

    void lagreUttalelseOmTilbakekreving(EttersendelseDto ettersendelse);
}
