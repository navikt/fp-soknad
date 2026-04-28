package no.nav.foreldrepenger.soknad.innsending;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolationException;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.OppholdsPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.OverføringsPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UtsettelsesPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public final class UttaksperioderValidator {

    private static final Logger LOG = LoggerFactory.getLogger(UttaksperioderValidator.class);

    private UttaksperioderValidator() {
    }

    public static void valider(SøknadDto søknad) {
        var uttaksperioder = switch (søknad) {
            case ForeldrepengesøknadDto fp -> fp.uttaksplan().uttaksperioder();
            case EndringssøknadForeldrepengerDto endring -> endring.uttaksplan().uttaksperioder();
            default -> null;
        };

        if (uttaksperioder == null) {
            return;
        }

        var søknadBeskrivelse = søknad instanceof EndringssøknadForeldrepengerDto endring ?
            "endringssøknad (saksnummer " + endring.saksnummer().value() + ")" : "førstegangssøknad";

        requireMinstEnPeriode(uttaksperioder, søknadBeskrivelse);
        requireAntallPerioderUnderTerskel(uttaksperioder, søknadBeskrivelse);
        requireFomFørTom(uttaksperioder, søknadBeskrivelse);
        requireIngenOverlapp(uttaksperioder, søknadBeskrivelse);
    }

    private static void requireMinstEnPeriode(List<Uttaksplanperiode> uttaksperioder, String søknadBeskrivelse) {
        if (uttaksperioder.isEmpty()) {
            LOG.warn("Mottatt {} uten uttaksperioder", søknadBeskrivelse);
            throw new ConstraintViolationException("Uttaksplan må inneholde minst én periode", Set.of());
        }
    }

    private static void requireAntallPerioderUnderTerskel(List<Uttaksplanperiode> uttaksperioder, String søknadBeskrivelse) {
        if (uttaksperioder.size() > 200) {
            LOG.warn("Mottatt {} med {} uttaksperioder (maks 200)", søknadBeskrivelse, uttaksperioder.size());
            throw new ConstraintViolationException("Uttaksplan kan ikke inneholde mer enn 200 perioder", Set.of());
        }
    }

    private static void requireFomFørTom(List<Uttaksplanperiode> uttaksperioder, String søknadBeskrivelse) {
        var perioderDerTomErFørFom = uttaksperioder.stream()
            .filter(p -> p.tom().isBefore(p.fom()))
            .map(UttaksperioderValidator::formatterPeriode)
            .toList();

        if (!perioderDerTomErFørFom.isEmpty()) {
            LOG.warn("Mottatt {} med uttaksperiode(r) der tom er før fom: {}", søknadBeskrivelse, String.join(", ", perioderDerTomErFørFom));
            throw new ConstraintViolationException("Uttaksplan inneholder perioder der tom er før fom", Set.of());
        }
    }

    private static void requireIngenOverlapp(List<Uttaksplanperiode> uttaksperioder, String søknadBeskrivelse) {
        try {
            var segmenter = uttaksperioder.stream().map(p -> new LocalDateSegment<>(p.fom(), p.tom(), formattertType(p))).toList();
            new LocalDateTimeline<>(segmenter);
        } catch (IllegalArgumentException e) {
            LOG.warn("Mottatt {} der uttaksperioder overlapper: {}", søknadBeskrivelse, e.getMessage());
            throw new ConstraintViolationException("Uttaksplan inneholder overlappende perioder", Set.of());
        }
    }

    private static String formatterPeriode(Uttaksplanperiode periode) {
        return String.format("{type=%s, fom=%s, tom=%s}", formattertType(periode), periode.fom(), periode.tom());
    }

    private static String formattertType(Uttaksplanperiode periode) {
        return switch (periode) {
            case UttaksPeriodeDto _ -> "uttak";
            case OverføringsPeriodeDto _ -> "overføring";
            case OppholdsPeriodeDto _ -> "opphold";
            case UtsettelsesPeriodeDto _ -> "utsettelse";
        };
    }
}
