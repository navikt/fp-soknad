package no.nav.foreldrepenger.mottak.domene.kontrakt.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum VedleggInnsendingType {
    @JsonEnumDefaultValue LASTET_OPP,
    SEND_SENERE,
    AUTOMATISK
}
