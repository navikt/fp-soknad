package no.nav.foreldrepenger.soknad.vedlegg.error;


import static java.lang.String.format;

import no.nav.foreldrepenger.soknad.vedlegg.VedleggUtil;


public class VedleggOpplastningTooLargeException extends VedleggOpplastningException {

    public VedleggOpplastningTooLargeException(long vedleggStørrelse, long maxTillattStørrelse) {
        super(format("Vedlegg-størrelse er %s MB, men kan ikke overstige %s MB",
            VedleggUtil.megabytes(vedleggStørrelse), VedleggUtil.megabytes(maxTillattStørrelse)));
    }

}
