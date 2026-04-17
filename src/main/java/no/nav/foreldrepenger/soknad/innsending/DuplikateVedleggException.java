package no.nav.foreldrepenger.soknad.innsending;

import no.nav.foreldrepenger.soknad.server.error.LokalFeilkode;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningException;

public class DuplikateVedleggException extends VedleggOpplastningException {
    public DuplikateVedleggException(String msg) {
        super(msg);
    }

    @Override
    public String getFeilkode() {
        return LokalFeilkode.DUPLIKAT_VEDLEGG.name();
    }
}
