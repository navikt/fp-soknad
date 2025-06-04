package no.nav.foreldrepenger.mottak.domene.kontrakt.dto.foreldrepenger.uttaksplan;

import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.MorsAktivitet;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.UtsettelsesÅrsak;

import java.time.LocalDate;

public record UtsettelsesPeriodeDto(@NotNull Type type,
                                    @NotNull LocalDate fom,
                                    @NotNull LocalDate tom,
                                    @NotNull UtsettelsesÅrsak årsak,
                                    MorsAktivitet morsAktivitetIPerioden,
                                    boolean erArbeidstaker) implements Uttaksplanperiode {

    public enum Type {
        UTSETTELSE,
        FRI
    }
}
