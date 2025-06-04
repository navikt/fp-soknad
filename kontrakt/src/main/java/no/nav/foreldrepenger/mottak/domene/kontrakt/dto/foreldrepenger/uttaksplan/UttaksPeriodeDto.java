package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.uttaksplan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.MorsAktivitet;

import java.time.LocalDate;

public record UttaksPeriodeDto(@NotNull LocalDate fom,
                               @NotNull LocalDate tom,
                               @NotNull KontoType konto,
                               MorsAktivitet morsAktivitetIPerioden,
                               Boolean ønskerSamtidigUttak,
                               Boolean justeresVedFødsel,
                               Boolean ønskerFlerbarnsdager,
                               @Min(0) @Max(100) Double samtidigUttakProsent) implements Uttaksplanperiode {
}
