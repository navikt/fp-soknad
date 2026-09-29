package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttakPeriodeDto;

public final class FpoversiktUttaksplanMapper {

    private FpoversiktUttaksplanMapper() {
    }

    public static Optional<FpoversiktUttaksplanRequest> map(String saksnummer,
                                                           LocalDateTime mottattTidspunkt,
                                                           List<UttakPeriodeDto> fellesUttaksplan) {
        if (fellesUttaksplan == null) {
            return Optional.empty();
        }
        var perioder = fellesUttaksplan.stream()
            .filter(periode -> periode.annenPart() != null)
            .map(periode -> new FpoversiktUttaksplanRequest.Periode(periode.fom(), periode.tom(), utenResultat(periode.annenPart())))
            .toList();
        return Optional.of(new FpoversiktUttaksplanRequest(saksnummer, mottattTidspunkt, perioder));
    }

    private static UttakPeriodeDto.UttakDto utenResultat(UttakPeriodeDto.UttakDto uttak) {
        var gradering = uttak.gradering() == null
            ? null
            : new UttakPeriodeDto.Gradering(uttak.gradering().arbeidstidprosent(), null);
        return new UttakPeriodeDto.UttakDto(uttak.forelder(), uttak.kontoType(), uttak.utsettelseÅrsak(), uttak.overføringÅrsak(),
            gradering, uttak.morsAktivitet(), uttak.samtidigUttak(), uttak.flerbarnsdager(), null);
    }
}
