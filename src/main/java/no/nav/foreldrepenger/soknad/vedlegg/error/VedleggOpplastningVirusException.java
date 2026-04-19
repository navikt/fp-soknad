package no.nav.foreldrepenger.soknad.vedlegg.error;


import java.util.UUID;

public class VedleggOpplastningVirusException extends VedleggOpplastningException {

    public VedleggOpplastningVirusException(UUID id) {
        super("Virus påvist i dokument med id " + id);
    }

}
