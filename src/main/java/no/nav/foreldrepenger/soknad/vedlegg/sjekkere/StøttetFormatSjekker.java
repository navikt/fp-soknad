package no.nav.foreldrepenger.soknad.vedlegg.sjekkere;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningTypeUnsupportedException;

@ApplicationScoped
public class StøttetFormatSjekker implements VedleggSjekker {
    public static final MediaType APPLICATION_PDF = MediaType.valueOf("application/pdf");
    public static final MediaType IMAGE_PNG = MediaType.valueOf("image/png");
    public static final MediaType IMAGE_JPEG = MediaType.valueOf("image/jpeg");
    private static final List<MediaType> supportedTypes = List.of(APPLICATION_PDF, IMAGE_PNG, IMAGE_JPEG);

    @Override
    public void sjekk(Vedlegg vedlegg) {
        var contentType = vedlegg.mediaType();
        if (contentType != null && !supportedTypes.contains(contentType)) {
            throw new VedleggOpplastningTypeUnsupportedException(contentType);
        }
    }
}
