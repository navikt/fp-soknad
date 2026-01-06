package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.Overføringsårsak;

public record OverføringsPeriodeDto(@NotNull LocalDate fom,
                                    @NotNull LocalDate tom,
                                    @NotNull Overføringsårsak årsak,
                                    @NotNull KontoType konto) implements Uttaksplanperiode {
}
