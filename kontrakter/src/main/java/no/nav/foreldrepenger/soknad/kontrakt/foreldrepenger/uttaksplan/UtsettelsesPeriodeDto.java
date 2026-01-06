package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.MorsAktivitet;

public record UtsettelsesPeriodeDto(@NotNull LocalDate fom,
                                    @NotNull LocalDate tom,
                                    @NotNull UtsettelsesÅrsak årsak,
                                    MorsAktivitet morsAktivitetIPerioden,
                                    boolean erArbeidstaker) implements Uttaksplanperiode {
}
