package no.nav.foreldrepenger.soknad.vedlegg.image2pdf;

import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.APPLICATION_PDF;
import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.IMAGE_JPEG;
import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.IMAGE_PNG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningTypeUnsupportedException;

class ImageByteArray2PDFConverterTest {
    private static Image2PDFConverter converter = new Image2PDFConverter();
    private static Tika tika = new Tika();

    @Test
    void jpgConvertsToPdf() {
        assertTrue(isPdf(converter.convert(hentVedleggFraResourcs("jks.jpg", IMAGE_JPEG))));
    }

    @Test
    void pngConvertsToPdf() {
        assertTrue(isPdf(converter.convert(hentVedleggFraResourcs("funny-christmas-memes-brace-yourself.png", IMAGE_PNG))));
    }

    @Test
    void gifFailsfWhenNotConfigured() {
        var vedlegg = hentVedleggFraResourcs("loading.gif", MediaType.valueOf("image/gif"));
        assertThrows(VedleggOpplastningTypeUnsupportedException.class, () -> converter.convert(vedlegg));
    }

    @Test
    void pdfRemainsUnchanged() {
        assertThat(isPdf(converter.convert(hentVedleggFraResourcs("es.pdf", APPLICATION_PDF)))).isTrue();
    }

    private static Vedlegg hentVedleggFraResourcs(String filnavn, MediaType mediaType) {
        try (var is = ImageByteArray2PDFConverterTest.class.getClassLoader().getResourceAsStream("vedlegg/" + filnavn)) {
            if (is == null) throw new RuntimeException("Resource not found: " + filnavn);
            return new Vedlegg(is.readAllBytes(), mediaType, filnavn, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static boolean isPdf(Vedlegg vedlegg) {
        var mediaType = MediaType.valueOf(tika.detect(vedlegg.bytes()));
        return APPLICATION_PDF.equals(mediaType);
    }

}
