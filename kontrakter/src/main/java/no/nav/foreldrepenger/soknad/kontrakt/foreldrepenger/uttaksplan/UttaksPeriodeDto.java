package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import static no.nav.foreldrepenger.kontrakter.felles.validering.InputValideringRegex.ORGNUMMER;
import static no.nav.foreldrepenger.kontrakter.felles.validering.InputValideringRegex.NORSK_FØDSELSNUMMER;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.MorsAktivitet;


public record UttaksPeriodeDto(@NotNull LocalDate fom,
                               @NotNull LocalDate tom,
                               @NotNull KontoType konto,
                               MorsAktivitet morsAktivitetIPerioden,
                               Boolean ønskerSamtidigUttak,
                               @Min(0) @Max(100) Double samtidigUttakProsent,
                               Boolean ønskerFlerbarnsdager,
                               Boolean ønskerGradering,
                               @Valid GraderingDto gradering) implements Uttaksplanperiode {

    public record GraderingDto(@NotNull @Min(0) @Max(100) Double stillingsprosent,
                               Boolean erArbeidstaker,
                               Boolean erFrilanser,
                               Boolean erSelvstendig,
                               @Size(max = 15) List<@Pattern(regexp = ORGNUMMER + "|" + NORSK_FØDSELSNUMMER, message = "orgnumre må være organisasjonummer eller personnummer") @NotNull String> orgnumre) {
    }

    @AssertTrue(message = "ønskerSamtidigUttak er satt, men ikke prosent, eller så er prosent satt og ikke ønskerSamtidigUttak")
    public boolean isSamtidigUttakGyldig() {
        if (Boolean.TRUE.equals(ønskerSamtidigUttak) && samtidigUttakProsent == null) {
            return false;
        }
        return true;
    }

}
