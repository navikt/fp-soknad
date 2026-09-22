package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;

public final class FpoversiktUttaksplanMapper {

    private FpoversiktUttaksplanMapper() {
    }

    public static Optional<FpoversiktUttaksplanRequest> map(String saksnummer,
                                                           LocalDateTime mottattTidspunkt,
                                                           FellesUttaksplanDto fellesUttaksplan) {
        if (fellesUttaksplan == null) {
            return Optional.empty();
        }
        var perioder = Optional.ofNullable(fellesUttaksplan.perioder()).orElse(List.of()).stream()
            .filter(periode -> periode.annenPart() != null)
            .map(periode -> new FpoversiktUttaksplanRequest.Periode(periode.fom(), periode.tom(), utenResultat(periode.annenPart())))
            .toList();
        return Optional.of(new FpoversiktUttaksplanRequest(saksnummer, mottattTidspunkt, perioder));
    }

    private static FellesUttaksplanDto.UttakDto utenResultat(FellesUttaksplanDto.UttakDto uttak) {
        var gradering = uttak.gradering() == null
            ? null
            : new FellesUttaksplanDto.Gradering(uttak.gradering().arbeidstidprosent(), null);
        return new FellesUttaksplanDto.UttakDto(uttak.forelder(), uttak.kontoType(), uttak.utsettelseÅrsak(), uttak.overføringÅrsak(),
            gradering, uttak.morsAktivitet(), uttak.samtidigUttak(), uttak.flerbarnsdager(), null);
    }
}
