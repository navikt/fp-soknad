package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.validering;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.VedleggDto;
import no.nav.foreldrepenger.mottak.domene.kontrakt.dto.VedleggInnsendingType;

public class VedlegglistestørrelseValidator implements ConstraintValidator<VedlegglistestørrelseConstraint, List<VedleggDto>> {

    @Override
    public boolean isValid(List<VedleggDto> values, ConstraintValidatorContext context) {
        var antallSendSenere = values.stream().filter(vf -> VedleggInnsendingType.SEND_SENERE.equals(vf.innsendingsType())).count();
        return antallSendSenere < 101 && (values.size() - antallSendSenere) < 41;
    }

}


