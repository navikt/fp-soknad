package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttakPeriodeDto;

public record FpoversiktUttaksplanRequest(String saksnummer,
                                         LocalDateTime mottattTidspunkt,
                                         List<Periode> perioder) {

    public record Periode(LocalDate fom, LocalDate tom, UttakPeriodeDto.UttakDto uttak) {
    }
}
