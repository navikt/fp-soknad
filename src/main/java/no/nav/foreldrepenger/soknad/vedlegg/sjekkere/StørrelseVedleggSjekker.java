package no.nav.foreldrepenger.soknad.vedlegg.sjekkere;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.AttachmentTooLargeException;
import no.nav.foreldrepenger.soknad.vedlegg.error.AttachmentUnreadableException;

@ApplicationScoped
public class StørrelseVedleggSjekker implements VedleggSjekker {
    private static final Logger LOG = LoggerFactory.getLogger(StørrelseVedleggSjekker.class);

    private final static int MAKS_STØRRELSE_ENKELT_VEDLEGG = 16 * 1024 * 1024; // 16 MB

    public StørrelseVedleggSjekker() {
    }

    @Override
    public void sjekk(Vedlegg vedlegg) {
        LOG.info("Sjekker størrelse for {}", vedlegg);
        var størrelseVedlegg = vedlegg.bytes().length;
        if (størrelseVedlegg == 0) {
            throw new AttachmentUnreadableException("Vedlegget er uten innhold");
        }
        if (størrelseVedlegg > MAKS_STØRRELSE_ENKELT_VEDLEGG) {
            throw new AttachmentTooLargeException(størrelseVedlegg, MAKS_STØRRELSE_ENKELT_VEDLEGG);
        }
    }

}
