package no.nav.foreldrepenger.soknad.vedlegg.image2pdf;

import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.IMAGE_JPEG;
import static no.nav.foreldrepenger.soknad.vedlegg.sjekkere.StøttetFormatSjekker.IMAGE_PNG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningImageTooLargeException;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningUnreadableException;

class ImageScalerTest {

    @Test
    void bilde_med_for_mange_piksler_avvises_uten_å_pakkes_ut() throws IOException {
        byte[] bombe;
        try (var is = getClass().getClassLoader().getResourceAsStream("vedlegg/dekompresjonsbombe-25000x25000.png")) {
            bombe = is.readAllBytes();
        }
        assertThat(bombe).hasSizeLessThan(100_000);

        assertThatThrownBy(() -> ImageScaler.lesBilde(bombe, IMAGE_PNG))
            .isInstanceOf(VedleggOpplastningImageTooLargeException.class)
            .hasMessageContaining("25000x25000")
            .extracting(e -> ((VedleggOpplastningImageTooLargeException) e).getFeilkode())
            .isEqualTo(LokalFeilkode.MELLOMLAGRING_VEDLEGG_BILDE_FOR_STORT.name());
    }

    @Test
    void stort_bilde_leses_inn_nedskalert() throws IOException {
        var bilde = ImageScaler.lesBilde(png(7500, 6000), IMAGE_PNG);

        assertThat(bilde.getWidth()).isEqualTo(3750);
        assertThat(bilde.getHeight()).isEqualTo(3000);
    }

    @Test
    void vanlig_mobilbilde_leses_inn_i_full_størrelse() throws IOException {
        var bilde = ImageScaler.lesBilde(png(4000, 3000), IMAGE_PNG);

        assertThat(bilde.getWidth()).isEqualTo(4000);
        assertThat(bilde.getHeight()).isEqualTo(3000);
    }

    @Test
    void subsampling_holder_bildet_minst_like_stort_som_A4_i_300_dpi() {
        var a4 = 2480L * 3508L;
        assertThat(ImageScaler.subsamplingFaktor(1)).isEqualTo(1);
        assertThat(ImageScaler.subsamplingFaktor(4 * a4 - 1)).isEqualTo(1);
        assertThat(ImageScaler.subsamplingFaktor(4 * a4)).isEqualTo(2);
        assertThat(ImageScaler.subsamplingFaktor(200_000_000L)).isEqualTo(4);
        assertThat(ImageScaler.subsamplingFaktor(ImageScaler.MAKS_ANTALL_PIKSLER)).isEqualTo(5);
    }

    @Test
    void uleselig_bilde_avvises() {
        var søppel = new byte[] { 1, 2, 3, 4, 5 };
        assertThatThrownBy(() -> ImageScaler.lesBilde(søppel, IMAGE_JPEG))
            .isInstanceOf(VedleggOpplastningUnreadableException.class);
    }

    private static byte[] png(int bredde, int høyde) throws IOException {
        var bilde = new BufferedImage(bredde, høyde, BufferedImage.TYPE_BYTE_BINARY);
        var out = new ByteArrayOutputStream();
        ImageIO.write(bilde, "png", out);
        return out.toByteArray();
    }
}
