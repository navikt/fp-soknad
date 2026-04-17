package no.nav.foreldrepenger.soknad.vedlegg.error;


import static no.nav.foreldrepenger.soknad.vedlegg.VedleggUtil.megabytes;

import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;

public class VedleggVirusscanTimeoutException extends VedleggOpplastningException {

    public VedleggVirusscanTimeoutException(Vedlegg vedlegg, Exception e) {
        super(feilmelding(vedlegg), e);
    }

    private static String feilmelding(Vedlegg vedlegg) {
        return "Virus-scan av %s vedlegg tok for lang tid (%sMB %s)".formatted(vedlegg.uuid(), megabytes(vedlegg.bytes().length), vedlegg.mediaType());
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.MELLOMLAGRING_VEDLEGG_VIRUSSCAN_TIMEOUT.name();
    }

}
