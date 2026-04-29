package no.nav.foreldrepenger.soknad.innsending.validering;

import java.util.List;

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

public final class UttaksperioderValidering {

    private static final int MAKS_ANTALL_PERIODER = 200;

    private UttaksperioderValidering() {
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
            var msg = String.format("Uttaksplan må inneholde minst én periode. Gjelder %s", søknadBeskrivelse);
            throw new UttaksperioderValideringException(msg);
        }
    }

    private static void requireAntallPerioderUnderTerskel(List<Uttaksplanperiode> uttaksperioder, String søknadBeskrivelse) {
        if (uttaksperioder.size() > MAKS_ANTALL_PERIODER) {
            var msg = String.format("Uttaksplan kan ikke inneholde mer enn %s perioder. Gjelder %s med %s uttaksperioder", MAKS_ANTALL_PERIODER, søknadBeskrivelse, uttaksperioder.size());
            throw new UttaksperioderValideringException(msg);
        }
    }

    private static void requireFomFørTom(List<Uttaksplanperiode> uttaksperioder, String søknadBeskrivelse) {
        var perioderDerTomErFørFom = uttaksperioder.stream()
            .filter(p -> p.tom().isBefore(p.fom()))
            .map(UttaksperioderValidering::formatPeriode)
            .toList();

        if (!perioderDerTomErFørFom.isEmpty()) {
            var msg = String.format("Uttaksplan inneholder perioder der tom er før fom. Gjelder %s: %s", søknadBeskrivelse,
                String.join(", ", perioderDerTomErFørFom));
            throw new UttaksperioderValideringException(msg);
        }
    }

    private static void requireIngenOverlapp(List<Uttaksplanperiode> uttaksperioder, String søknadBeskrivelse) {
        try {
            var segmenter = uttaksperioder.stream().map(p -> new LocalDateSegment<>(p.fom(), p.tom(), formatPeriodetype(p))).toList();
            new LocalDateTimeline<>(segmenter);
        } catch (IllegalArgumentException e) {
            var msg = String.format("Uttaksplan inneholder overlappende perioder. Gjelder %s: %s", søknadBeskrivelse, e.getMessage());
            throw new UttaksperioderValideringException(msg);
        }
    }

    private static String formatPeriode(Uttaksplanperiode periode) {
        return String.format("{type=%s, fom=%s, tom=%s}", formatPeriodetype(periode), periode.fom(), periode.tom());
    }

    private static String formatPeriodetype(Uttaksplanperiode periode) {
        return switch (periode) {
            case UttaksPeriodeDto _ -> "uttak";
            case OverføringsPeriodeDto _ -> "overføring";
            case OppholdsPeriodeDto _ -> "opphold";
            case UtsettelsesPeriodeDto _ -> "utsettelse";
        };
    }
}
