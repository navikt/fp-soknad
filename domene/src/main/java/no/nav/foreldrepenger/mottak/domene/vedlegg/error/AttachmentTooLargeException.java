package no.nav.foreldrepenger.mottak.domene.vedlegg.error;


import no.nav.foreldrepenger.mottak.domene.vedlegg.VedleggUtil;

import static java.lang.String.format;


public class AttachmentTooLargeException extends AttachmentException {

    public AttachmentTooLargeException(long vedleggStørrelse, long maxTillattStørrelse) {
        super(format("Vedlegg-størrelse er %s MB, men kan ikke overstige %s MB",
            VedleggUtil.megabytes(vedleggStørrelse), VedleggUtil.megabytes(maxTillattStørrelse)));
    }

}
