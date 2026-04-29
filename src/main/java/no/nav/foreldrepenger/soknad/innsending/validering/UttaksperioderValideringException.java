package no.nav.foreldrepenger.soknad.innsending.validering;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.feil.Feilkode;


public class UttaksperioderValideringException extends FunksjonellException {

    public UttaksperioderValideringException(String msg) {
        super(null, msg);
    }

    @Override
    public String getFeilkode() {
        return Feilkode.VALIDERING.name();
    }

}
