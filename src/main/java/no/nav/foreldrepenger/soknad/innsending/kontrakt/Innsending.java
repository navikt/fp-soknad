package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.time.LocalDate;
import java.util.List;


public interface Innsending {
    String navn();

    LocalDate mottattdato();

    List<VedleggDto> vedlegg();

    default List<VedleggDto> påkrevdeVedlegg() {
        return vedlegg().stream().filter(v -> !VedleggInnsendingType.AUTOMATISK.equals(v.innsendingsType())).toList();
    }
}
