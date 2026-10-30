package no.nav.foreldrepenger.soknad.innsending.validering;

import static no.nav.foreldrepenger.soknad.innsending.fordel.utils.FpsakUttaksperioder.søkersPerioder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public final class UttaksperioderValidering {

    private static final int MAKS_ANTALL_PERIODER_TOTALT = 400;
    private static final int MAKS_ANTALL_SØKERPERIODER = 240;

    private UttaksperioderValidering() {
    }

    public static void valider(SøknadDto søknad) {
        var uttaksplan = switch (søknad) {
            case ForeldrepengesøknadDto fp -> fp.uttaksplan();
            case EndringssøknadForeldrepengerDto endring -> endring.uttaksplan();
            default -> null;
        };

        if (uttaksplan == null) {
            return;
        }

        var søknadBeskrivelse = søknad instanceof EndringssøknadForeldrepengerDto endring ?
            "endringssøknad (saksnummer " + endring.saksnummer().value() + ")" : "førstegangssøknad";

        if (uttaksplan.perioder() == null) {
            throw new UttaksperioderValideringException("Uttaksplan mangler perioder. Gjelder " + søknadBeskrivelse);
        }
        // Med endringsdato legger EndringsdatoFilter alltid inn en søkerperiode, så kravet om minst én gjelder ikke.
        var harEndringsdato = søknad instanceof EndringssøknadForeldrepengerDto e && e.endringsdato() != null;
        validerPerioder(uttaksplan.perioder(), søknadBeskrivelse, !harEndringsdato);
        if (søknad instanceof EndringssøknadForeldrepengerDto endring && endring.endringsdato() != null) {
            if (endring.rolle() == null) {
                // Rollen trengs for å legge inn fri utsettelse på endringsdato
                throw new UttaksperioderValideringException("Endringssøknad med endringsdato mangler rolle. Gjelder " + søknadBeskrivelse);
            }
            validerEndringsdato(uttaksplan.perioder(), endring.endringsdato(), søknadBeskrivelse);
        }
    }

    private static void validerEndringsdato(List<UttakPeriodeDto> perioder, LocalDate endringsdato, String søknadBeskrivelse) {
        // FPSAK fjerner perioder uten virkedager. Fri utsettelse på endringsdato må derfor starte på en virkedag.
        if (endringsdato.getDayOfWeek() == DayOfWeek.SATURDAY || endringsdato.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new UttaksperioderValideringException(String.format(
                "Endringsdato %s er i helg. Gjelder %s", endringsdato, søknadBeskrivelse));
        }
        // Frontend deler perioder på endringsdato. Ingen søkerperiode kan krysse den, fordi EndringsdatoFilter ikke deler.
        // Starter ingen søkerperiode på endringsdato, legger EndringsdatoFilter inn fri utsettelse.
        var kryssende = søkersPerioder(perioder).stream()
            .filter(p -> p.fom().isBefore(endringsdato) && !p.tom().isBefore(endringsdato))
            .findAny();
        if (kryssende.isPresent()) {
            throw new UttaksperioderValideringException(String.format(
                "Endringsdato %s ligger inne i en søkerperiode. Gjelder %s", endringsdato, søknadBeskrivelse));
        }
    }

    private static void validerPerioder(List<UttakPeriodeDto> perioder, String søknadBeskrivelse, boolean kreverSøkerperiode) {
        requireAntallUnderTerskel(perioder.size(), MAKS_ANTALL_PERIODER_TOTALT, "perioder totalt");
        var antallSøkerperioder = perioder.stream().filter(p -> p.søker() != null).count();
        requireAntallUnderTerskel((int) antallSøkerperioder, MAKS_ANTALL_SØKERPERIODER, "søkerperioder");
        var søkerperioder = søkersPerioder(perioder);
        if (kreverSøkerperiode) {
            requireMinstEnPeriode(søkerperioder, søknadBeskrivelse);
        }
        var ugyldigeDatoer = perioder.stream()
            .filter(p -> p.tom().isBefore(p.fom()))
            .map(p -> String.format("{fom=%s, tom=%s}", p.fom(), p.tom()))
            .toList();
        if (!ugyldigeDatoer.isEmpty()) {
            throw new UttaksperioderValideringException(String.format(
                "Uttaksplan inneholder perioder der tom er før fom. Gjelder %s: %s",
                søknadBeskrivelse, String.join(", ", ugyldigeDatoer)));
        }
        for (var periode : søkerperioder) {
            var søker = periode.søker();
            if (søker != null && søker.utsettelseÅrsak() == null && søker.kontoType() == null) {
                throw new UttaksperioderValideringException(
                    "Søkerperiode må ha konto eller utsettelsesårsak. Gjelder " + søknadBeskrivelse);
            }
        }
        try {
            var søkerSegmenter = søkerperioder.stream()
                .map(p -> new LocalDateSegment<>(p.fom(), p.tom(), "søker"))
                .toList();
            new LocalDateTimeline<>(søkerSegmenter);
            var annenPartSegmenter = perioder.stream()
                .filter(p -> p.annenPart() != null)
                .map(p -> new LocalDateSegment<>(p.fom(), p.tom(), "annenPart"))
                .toList();
            new LocalDateTimeline<>(annenPartSegmenter);
        } catch (IllegalArgumentException e) {
            throw new UttaksperioderValideringException(String.format(
                "Uttaksplan inneholder overlappende perioder. Gjelder %s: %s", søknadBeskrivelse, e.getMessage()));
        }
    }

    private static void requireMinstEnPeriode(List<?> uttaksperioder, String søknadBeskrivelse) {
        if (uttaksperioder.isEmpty()) {
            var msg = String.format("Uttaksplan må inneholde minst én periode. Gjelder %s", søknadBeskrivelse);
            throw new UttaksperioderValideringException(msg);
        }
    }

    private static void requireAntallUnderTerskel(int antall, int grense, String periodeType) {
        if (antall > grense) {
            var msg = String.format("Uttaksplan kan ikke inneholde mer enn %s %s (antall: %s)", grense, periodeType, antall);
            throw new UttaksperioderValideringException(msg);
        }
    }

}
