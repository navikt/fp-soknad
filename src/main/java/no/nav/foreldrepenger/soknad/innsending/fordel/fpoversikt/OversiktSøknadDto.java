package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

public record OversiktSøknadDto(Saksnummer saksnummer,
                                LocalDateTime mottattTidspunkt,
                                Rolle rolle,
                                List<UttakPeriodeDto> perioder) {
    @Override
    public String toString() {
        return "OversiktSøknadDto[skjermet]";
    }
}
