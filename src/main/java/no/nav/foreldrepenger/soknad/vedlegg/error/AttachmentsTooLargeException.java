package no.nav.foreldrepenger.soknad.vedlegg.error;

import no.nav.foreldrepenger.soknad.vedlegg.VedleggUtil;

import static java.lang.String.format;

public class AttachmentsTooLargeException extends AttachmentException {

    public AttachmentsTooLargeException(long vedleggStørrelse, long maxTillattStørrelse) {
        super(format("Samlet filstørrelse for alle vedlegg er %s Mb, men må være mindre enn %s"
        , VedleggUtil.megabytes(vedleggStørrelse), VedleggUtil.megabytes(maxTillattStørrelse)));
    }
}
