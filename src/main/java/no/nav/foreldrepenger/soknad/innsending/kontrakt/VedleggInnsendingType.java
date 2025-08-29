package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum VedleggInnsendingType {
    @JsonEnumDefaultValue LASTET_OPP,
    SEND_SENERE,
    AUTOMATISK
}
