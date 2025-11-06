package no.nav.foreldrepenger.soknad.vedlegg.image2pdf;


import static no.nav.foreldrepenger.soknad.vedlegg.image2pdf.ImageScaler.pdfFraBilde;
import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.APPLICATION_PDF;
import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.IMAGE_JPEG;
import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.IMAGE_PNG;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningConversionException;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningTypeUnsupportedException;

@ApplicationScoped
public class Image2PDFConverter {
    private static final Logger LOG = LoggerFactory.getLogger(Image2PDFConverter.class);

    private final List<MediaType> supportedMediaTypes;

    public Image2PDFConverter() {
        this(List.of(IMAGE_JPEG, IMAGE_PNG, APPLICATION_PDF));
    }

    Image2PDFConverter(List<MediaType> supportedMediaTypes) {
        this.supportedMediaTypes = supportedMediaTypes;
    }


    public Vedlegg convert(Vedlegg vedlegg) {
        var mediaType = vedlegg.mediaType();
        if (APPLICATION_PDF.equals(mediaType)) {
            return vedlegg;
        }

        if (supportedMediaTypes.contains(mediaType)) {
            var start = System.currentTimeMillis();
            var pdfBytes = konverterBildeTilPdf(vedlegg.bytes(), mediaType);
            var slutt = System.currentTimeMillis();
            LOG.info("Konvertering av {} til PDF tok {}ms", mediaType, slutt - start);
            return new Vedlegg(pdfBytes, APPLICATION_PDF, vedlegg.filnavn(), vedlegg.uuid());
        }
        throw new VedleggOpplastningTypeUnsupportedException(mediaType);
    }

    private static byte[] konverterBildeTilPdf(byte[] innhold, MediaType mediaType) {
        try (var doc = new PDDocument(); var outputStream = new ByteArrayOutputStream()) {
            addPDFPageFromImage(doc, innhold, mediaType);
            doc.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new VedleggOpplastningConversionException(mediaType, e);
        }
    }

    private static void addPDFPageFromImage(PDDocument doc, byte[] orig, MediaType mediaType) {
        try {
           pdfFraBilde(doc, orig, mediaType);
        } catch (Exception e) {
            throw new VedleggOpplastningConversionException(mediaType, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [supportedMediaTypes=" + supportedMediaTypes + "]";
    }
}
