package no.nav.foreldrepenger.soknad.vedlegg.sjekkere;

import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.APPLICATION_PDF;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningPasswordProtectedException;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningUnreadableException;


@ApplicationScoped
public class PDFEncryptionVedleggSjekker implements VedleggSjekker {

    private static final Logger LOG = LoggerFactory.getLogger(PDFEncryptionVedleggSjekker.class);

    @Override
    public void sjekk(Vedlegg vedlegg) {
        var innhold = vedlegg.bytes();
        if (innhold != null && APPLICATION_PDF.equals(vedlegg.mediaType())) {
            try (var _ = Loader.loadPDF(innhold)) {
                // Check to read PDF, and close it automatically
            } catch (InvalidPasswordException e) {
                LOG.info("Pdf feiler sjekk for kryptering", e);
                throw new VedleggOpplastningPasswordProtectedException(vedlegg.mediaType());
            } catch (Exception e) {
                throw new VedleggOpplastningUnreadableException("Pdf er uleselig", vedlegg.mediaType(), e);
            }
        }
    }

}
