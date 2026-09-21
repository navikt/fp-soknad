package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.kontrakter.felles.kodeverk.Overføringsårsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Aktivitet;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Gradering;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

public final class FellesUttaksplanMapper {

    private FellesUttaksplanMapper() {
    }

    public static UttaksplanDto tilUttaksplan(FellesUttaksplanDto fellesUttaksplan, UttaksplanDto legacyUttaksplan) {
        var perioder = mapPerioder(fellesUttaksplan, true);
        return new UttaksplanDto(ønskerJustertUttakVedFødsel(legacyUttaksplan), perioder);
    }

    public static UttaksplanDto tilUttaksplanForEndringssøknad(FellesUttaksplanDto fellesUttaksplan, UttaksplanDto legacyUttaksplan) {
        var cutoff = finnEndringstidspunkt(legacyUttaksplan);
        var perioder = new ArrayList<>(mapPerioder(fellesUttaksplan, false).stream()
            .filter(periode -> periode.tom() != null && !periode.tom().isBefore(cutoff))
            .toList());
        finnFriEndringsmarkør(legacyUttaksplan, cutoff).ifPresent(perioder::add);
        perioder.sort(Comparator.comparing(Uttaksplanperiode::fom));
        return new UttaksplanDto(ønskerJustertUttakVedFødsel(legacyUttaksplan), perioder);
    }

    private static List<Uttaksplanperiode> mapPerioder(FellesUttaksplanDto fellesUttaksplan, boolean inkluderAvslåttePerioder) {
        return Optional.ofNullable(fellesUttaksplan.perioder()).orElse(List.of()).stream()
            .filter(Objects::nonNull)
            .filter(periode -> inkluderAvslåttePerioder || erIkkeAvslått(periode.søker()))
            .map(FellesUttaksplanMapper::tilUttaksplanperiode)
            .flatMap(Optional::stream)
            .toList();
    }

    private static boolean erIkkeAvslått(UttakDto søker) {
        return søker == null || søker.resultat() == null || søker.resultat().innvilget();
    }

    private static LocalDate finnEndringstidspunkt(UttaksplanDto legacyUttaksplan) {
        if (legacyUttaksplan == null || legacyUttaksplan.uttaksperioder() == null || legacyUttaksplan.uttaksperioder().isEmpty()) {
            throw new IllegalArgumentException("Endringssøknad med fellesUttaksplan krever en ikke-tom legacy uttaksplan");
        }
        return legacyUttaksplan.uttaksperioder().stream()
            .map(Uttaksplanperiode::fom)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElseThrow(() -> new IllegalArgumentException("Legacy uttaksplan må ha minst én periode med fom"));
    }

    private static Optional<Uttaksplanperiode> finnFriEndringsmarkør(UttaksplanDto legacyUttaksplan, LocalDate cutoff) {
        return legacyUttaksplan.uttaksperioder().stream()
            .filter(periode -> cutoff.equals(periode.fom()))
            .filter(UtsettelsesPeriodeDto.class::isInstance)
            .filter(periode -> ((UtsettelsesPeriodeDto) periode).årsak() == UtsettelsesÅrsak.FRI)
            .findFirst();
    }

    private static Boolean ønskerJustertUttakVedFødsel(UttaksplanDto legacyUttaksplan) {
        return legacyUttaksplan == null ? null : legacyUttaksplan.ønskerJustertUttakVedFødsel();
    }

    private static Optional<Uttaksplanperiode> tilUttaksplanperiode(UttakPeriodeDto periode) {
        var søker = periode.søker();
        if (søker == null) {
            return Optional.empty();
        }
        if (søker.utsettelseÅrsak() != null) {
            return tilUtsettelsesperiode(periode, søker);
        }
        if (søker.overføringÅrsak() != null) {
            return tilOverføringsperiode(periode, søker);
        }
        if (søker.kontoType() == null) {
            return Optional.empty();
        }
        return Optional.of(tilUttaksperiode(periode, søker));
    }

    private static UttaksPeriodeDto tilUttaksperiode(UttakPeriodeDto periode, UttakDto søker) {
        var gradering = tilGradering(søker.gradering());
        var samtidigUttak = søker.samtidigUttak();
        return new UttaksPeriodeDto(periode.fom(),
            periode.tom(),
            søker.kontoType(),
            søker.morsAktivitet(),
            samtidigUttak == null ? null : true,
            samtidigUttak == null || samtidigUttak.value() == null ? null : samtidigUttak.value().doubleValue(),
            søker.flerbarnsdager(),
            gradering == null ? null : true,
            gradering);
    }

    private static UttaksPeriodeDto.GraderingDto tilGradering(Gradering gradering) {
        if (gradering == null) {
            return null;
        }
        if (gradering.arbeidstidprosent() == null || gradering.arbeidstidprosent().value() == null) {
            return null;
        }
        var aktivitet = gradering.aktivitet();
        var arbeidsgiver = aktivitet == null ? null : aktivitet.arbeidsgiver();
        return new UttaksPeriodeDto.GraderingDto(gradering.arbeidstidprosent().value().doubleValue(),
            harAktivitetstype(aktivitet, Aktivitet.AktivitetType.ORDINÆRT_ARBEID),
            harAktivitetstype(aktivitet, Aktivitet.AktivitetType.FRILANS),
            harAktivitetstype(aktivitet, Aktivitet.AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE),
            arbeidsgiver != null && arbeidsgiver.id() != null ? List.of(arbeidsgiver.id()) : List.of());
    }

    private static boolean harAktivitetstype(Aktivitet aktivitet, Aktivitet.AktivitetType type) {
        return aktivitet != null && aktivitet.type() == type;
    }

    private static Optional<Uttaksplanperiode> tilOverføringsperiode(UttakPeriodeDto periode, UttakDto søker) {
        if (søker.kontoType() == null) {
            return Optional.empty();
        }
        var årsak = Overføringsårsak.valueOf(søker.overføringÅrsak().name());
        return Optional.of(new OverføringsPeriodeDto(periode.fom(), periode.tom(), årsak, søker.kontoType()));
    }

    private static Optional<Uttaksplanperiode> tilUtsettelsesperiode(UttakPeriodeDto periode, UttakDto søker) {
        var årsak = switch (søker.utsettelseÅrsak()) {
            case SØKER_SYKDOM -> UtsettelsesÅrsak.SYKDOM;
            case SØKER_INNLAGT -> UtsettelsesÅrsak.INSTITUSJONSOPPHOLD_SØKER;
            case BARN_INNLAGT -> UtsettelsesÅrsak.INSTITUSJONSOPPHOLD_BARNET;
            case HV_ØVELSE -> UtsettelsesÅrsak.HV_OVELSE;
            case NAV_TILTAK -> UtsettelsesÅrsak.NAV_TILTAK;
            case ARBEID, FERIE, FRI -> null;
        };
        return årsak == null
            ? Optional.empty()
            : Optional.of(new UtsettelsesPeriodeDto(periode.fom(), periode.tom(), årsak, søker.morsAktivitet(), false));
    }
}
