package no.nav.foreldrepenger.soknad.vedlegg.sjekkere;

import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.APPLICATION_PDF;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.AttachmentPasswordProtectedException;
import no.nav.foreldrepenger.soknad.vedlegg.error.AttachmentUnreadableException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class PDFEncryptionVedleggSjekker implements VedleggSjekker {

    private static final Logger LOG = LoggerFactory.getLogger(PDFEncryptionVedleggSjekker.class);

    @Override
    public void sjekk(Vedlegg vedlegg) {
        var innhold = vedlegg.bytes();
        if (innhold != null && APPLICATION_PDF.equals(vedlegg.mediaType())) {
            try (var doc = Loader.loadPDF(innhold)) {
            } catch (InvalidPasswordException e) {
                LOG.info("Pdf feiler sjekk for kryptering", e);
                throw new AttachmentPasswordProtectedException();
            } catch (Exception e) {
                throw new AttachmentUnreadableException("Pdf er uleselig");
            }
        }
    }

}
