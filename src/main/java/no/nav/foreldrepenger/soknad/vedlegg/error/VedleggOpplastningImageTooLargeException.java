package no.nav.foreldrepenger.soknad.vedlegg.error;

import static java.lang.String.format;

import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;

public class VedleggOpplastningImageTooLargeException extends VedleggOpplastningException {

    public VedleggOpplastningImageTooLargeException(long bredde, long høyde, long maksAntallPiksler, MediaType mediaType) {
        super(format("Bildet er %sx%s piksler, men kan ikke ha flere enn %s piksler", bredde, høyde, maksAntallPiksler), mediaType);
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.MELLOMLAGRING_VEDLEGG_BILDE_FOR_STORT.name();
    }
}
