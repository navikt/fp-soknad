package no.nav.foreldrepenger.mottak.domene.innsending;

import no.nav.foreldrepenger.common.domain.Søknad;
import no.nav.foreldrepenger.mottak.domene.mellomlagring.MellomlagringTjeneste;
import no.nav.foreldrepenger.mottak.domene.utils.InnloggetBruker;
import no.nav.foreldrepenger.mottak.domene.vedlegg.VedleggTjeneste;

public class SøknadInnsendingTjeneste {

    private MellomlagringTjeneste mellomlagringTjeneste;
    private VedleggTjeneste vedleggTjeneste;
    private InnloggetBruker innloggetBruker;


    public void lagreInnsending(Søknad søknad) {
        /**
         * 1) Hent vedlegg fra mellomlagring
         * 2) Lagre søknad og vedlegg i database
         * 3) Prosesstask for videre prosessering
         */
    }
}
