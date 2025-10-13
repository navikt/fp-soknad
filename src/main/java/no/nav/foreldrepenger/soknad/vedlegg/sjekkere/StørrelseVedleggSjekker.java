package no.nav.foreldrepenger.soknad.vedlegg.sjekkere;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningTooLargeException;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningUnreadableException;

@ApplicationScoped
public class StørrelseVedleggSjekker implements VedleggSjekker {
    private static final Logger LOG = LoggerFactory.getLogger(StørrelseVedleggSjekker.class);

    private static final int MAKS_STØRRELSE_ENKELT_VEDLEGG = 16 * 1024 * 1024; // 16 MB

    public StørrelseVedleggSjekker() {
        // CDI
    }

    @Override
    public void sjekk(Vedlegg vedlegg) {
        LOG.info("Sjekker størrelse for {}", vedlegg);
        var størrelseVedlegg = vedlegg.bytes().length;
        if (størrelseVedlegg == 0) {
            throw new VedleggOpplastningUnreadableException("Vedlegget er uten innhold", vedlegg.mediaType());
        }
        if (størrelseVedlegg > MAKS_STØRRELSE_ENKELT_VEDLEGG) {
            throw new VedleggOpplastningTooLargeException(størrelseVedlegg, MAKS_STØRRELSE_ENKELT_VEDLEGG);
        }
    }

}
