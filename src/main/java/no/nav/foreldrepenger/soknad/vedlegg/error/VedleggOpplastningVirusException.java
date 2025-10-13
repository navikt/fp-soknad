package no.nav.foreldrepenger.soknad.vedlegg.error;


public class VedleggOpplastningVirusException extends VedleggOpplastningException {

    public VedleggOpplastningVirusException(String id) {
        super("Virus påvist i dokument med id " + id, null, null);
    }

}
