package no.nav.foreldrepenger.mottak.domene.vedlegg.sjekkere;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.mottak.domene.vedlegg.Vedlegg;
import no.nav.foreldrepenger.mottak.domene.vedlegg.error.AttachmentTooLargeException;
import no.nav.foreldrepenger.mottak.domene.vedlegg.error.AttachmentUnreadableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StørrelseVedleggSjekker implements VedleggSjekker {
    private static final Logger LOG = LoggerFactory.getLogger(StørrelseVedleggSjekker.class);

    private final static int MAKS_STØRRELSE_ENKELT_VEDLEGG = 16 * 1024 * 1024; // 16 MB
    private final static int MAKS_STØRRELSE_TOTALT_ALLE_VEDLEGG = 64 * 1024 * 1024; // 64 MB

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

    // TODO: Total bør sjekkes, men ikke hers
//    private void sjekkTotalStørrelse(Vedlegg vedlegg) {
//        LOG.info("Sjekker total størrelse for {} vedlegg", vedlegg.bytes().length);
//        var total = safeStream(vedlegg)
//            .map(Vedlegg::bytes)
//            .mapToLong(v -> v.length)
//            .sum();
//        if (total > MAKS_STØRRELSE_TOTALT_ALLE_VEDLEGG) {
//            throw new AttachmentsTooLargeException(total, MAKS_STØRRELSE_TOTALT_ALLE_VEDLEGG);
//        }
//    }

}
