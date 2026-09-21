package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FEDREKVOTE;
import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.MorsAktivitet.ARBEID;
import static no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle.MOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Aktivitet;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Arbeidsgiver;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Arbeidstidprosent;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.EøsUttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Gradering;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.OverføringÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.SamtidigUttak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UtsettelseÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.VedtattResultat;

class FellesUttaksplanMapperTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 5);

    @Test
    void mapper_søkers_relevante_perioder_til_legacy_uttaksplan() {
        var gradering = new Gradering(new Arbeidstidprosent(BigDecimal.valueOf(40)),
            new Aktivitet(Aktivitet.AktivitetType.ORDINÆRT_ARBEID,
                new Arbeidsgiver("999999999", Arbeidsgiver.ArbeidsgiverType.ORGANISASJON), "Arbeidsgiver"));
        var resultat = new VedtattResultat(true, false, true, VedtattResultat.Årsak.ANNET);
        var uttak = new UttakDto(MOR, FELLESPERIODE, null, null, gradering, ARBEID,
            new SamtidigUttak(BigDecimal.valueOf(20)), true, resultat);
        var overføring = new UttakDto(MOR, FEDREKVOTE, null, OverføringÅrsak.SYKDOM_ANNEN_FORELDER,
            null, null, null, false, null);

        var perioder = List.of(
            periode(0, uttak, null, null),
            periode(1, overføring, null, null),
            periode(2, utsettelse(UtsettelseÅrsak.SØKER_SYKDOM), null, null),
            periode(3, utsettelse(UtsettelseÅrsak.SØKER_INNLAGT), null, null),
            periode(4, utsettelse(UtsettelseÅrsak.BARN_INNLAGT), null, null),
            periode(5, utsettelse(UtsettelseÅrsak.HV_ØVELSE), null, null),
            periode(6, utsettelse(UtsettelseÅrsak.NAV_TILTAK), null, null),
            periode(7, utsettelse(UtsettelseÅrsak.ARBEID), null, null),
            periode(8, utsettelse(UtsettelseÅrsak.FERIE), null, null),
            periode(9, utsettelse(UtsettelseÅrsak.FRI), null, null),
            periode(10, null, uttak, null),
            periode(11, null, null, new EøsUttakDto(FELLESPERIODE, new EøsUttakDto.Trekkdager(BigDecimal.ONE))));
        var fellesPlan = new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE, perioder);

        var legacyPlan = FellesUttaksplanMapper.tilUttaksplan(fellesPlan, null);

        assertThat(legacyPlan.ønskerJustertUttakVedFødsel()).isNull();
        assertThat(legacyPlan.uttaksperioder()).hasSize(7);

        var uttaksperiode = (UttaksPeriodeDto) legacyPlan.uttaksperioder().getFirst();
        assertThat(uttaksperiode.konto()).isEqualTo(FELLESPERIODE);
        assertThat(uttaksperiode.morsAktivitetIPerioden()).isEqualTo(ARBEID);
        assertThat(uttaksperiode.ønskerSamtidigUttak()).isTrue();
        assertThat(uttaksperiode.samtidigUttakProsent()).isEqualTo(20);
        assertThat(uttaksperiode.ønskerFlerbarnsdager()).isTrue();
        assertThat(uttaksperiode.ønskerGradering()).isTrue();
        assertThat(uttaksperiode.gradering().stillingsprosent()).isEqualTo(40);
        assertThat(uttaksperiode.gradering().erArbeidstaker()).isTrue();
        assertThat(uttaksperiode.gradering().orgnumre()).containsExactly("999999999");

        var overføringsperiode = (OverføringsPeriodeDto) legacyPlan.uttaksperioder().get(1);
        assertThat(overføringsperiode.årsak().name()).isEqualTo(OverføringÅrsak.SYKDOM_ANNEN_FORELDER.name());
        assertThat(overføringsperiode.konto()).isEqualTo(FEDREKVOTE);

        assertThat(legacyPlan.uttaksperioder().subList(2, 7))
            .extracting(UtsettelsesPeriodeDto.class::cast)
            .extracting(UtsettelsesPeriodeDto::årsak)
            .containsExactly(
                UtsettelsesÅrsak.SYKDOM,
                UtsettelsesÅrsak.INSTITUSJONSOPPHOLD_SØKER,
                UtsettelsesÅrsak.INSTITUSJONSOPPHOLD_BARNET,
                UtsettelsesÅrsak.HV_OVELSE,
                UtsettelsesÅrsak.NAV_TILTAK);
    }

    @Test
    void validering_kaskaderer_til_perioder_og_underliggende_dtoer() {
        var ugyldigResultat = new VedtattResultat(true, false, true, null);
        var ugyldigSøker = new UttakDto(null, FELLESPERIODE, null, null, null, null, null, false, ugyldigResultat);
        var ugyldigEøsUttak = new EøsUttakDto(FELLESPERIODE, new EøsUttakDto.Trekkdager(BigDecimal.valueOf(-1)));
        var plan = new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE,
            List.of(periode(0, ugyldigSøker, null, ugyldigEøsUttak)));

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var valideringsfeil = factory.getValidator().validate(plan);

            assertThat(valideringsfeil)
                .extracting(feil -> feil.getPropertyPath().toString())
                .contains(
                    "perioder[0].søker.forelder",
                    "perioder[0].søker.resultat.årsak",
                    "perioder[0].annenPartEøs.trekkdager.verdi");
        }
    }

    @Test
    void endringssøknad_avgrenser_full_plan_fra_legacy_cutoff_og_utelater_avslåtte_perioder() {
        var cutoff = START.plusWeeks(2);
        var legacyPeriode = new UttaksPeriodeDto(cutoff, cutoff.plusDays(4), FELLESPERIODE, null, null, null, false, null, null);
        var legacyPlan = new UttaksplanDto(true, List.of(legacyPeriode));
        var innvilget = new VedtattResultat(true, false, true, VedtattResultat.Årsak.ANNET);
        var avslått = new VedtattResultat(false, false, false, VedtattResultat.Årsak.ANNET);
        var plan = new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE, List.of(
            uttaksperiode(START, START.plusDays(4), innvilget),
            uttaksperiode(cutoff.minusDays(2), cutoff.plusDays(2), innvilget),
            uttaksperiode(cutoff.plusWeeks(1), cutoff.plusWeeks(1).plusDays(4), innvilget),
            uttaksperiode(cutoff.plusWeeks(2), cutoff.plusWeeks(2).plusDays(4), avslått)));

        var førstegang = FellesUttaksplanMapper.tilUttaksplan(plan, null);
        var resultat = FellesUttaksplanMapper.tilUttaksplanForEndringssøknad(plan, legacyPlan);

        assertThat(førstegang.uttaksperioder()).hasSize(4);
        assertThat(resultat.ønskerJustertUttakVedFødsel()).isTrue();
        assertThat(resultat.uttaksperioder())
            .extracting(Uttaksplanperiode::fom)
            .containsExactly(cutoff.minusDays(2), cutoff.plusWeeks(1));
    }

    @Test
    void endringssøknad_med_felles_plan_krever_legacy_perioder() {
        var plan = new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE, List.of());

        assertThatThrownBy(() -> FellesUttaksplanMapper.tilUttaksplanForEndringssøknad(plan, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ikke-tom legacy uttaksplan");
        assertThatThrownBy(() -> FellesUttaksplanMapper.tilUttaksplanForEndringssøknad(plan, new UttaksplanDto(null, List.of())))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ikke-tom legacy uttaksplan");
    }

    @Test
    void endringssøknad_beholder_fri_perioden_som_markerer_endringstidspunktet() {
        var cutoff = START.plusWeeks(2);
        var endringsmarkør = new UtsettelsesPeriodeDto(cutoff, cutoff.plusDays(4), UtsettelsesÅrsak.FRI, null, false);
        var legacyPlan = new UttaksplanDto(null, List.of(endringsmarkør));
        var plan = new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE, List.of(
            uttaksperiode(START, START.plusDays(4), null),
            uttaksperiode(cutoff.plusWeeks(1), cutoff.plusWeeks(1).plusDays(4), null)));

        var resultat = FellesUttaksplanMapper.tilUttaksplanForEndringssøknad(plan, legacyPlan);

        assertThat(resultat.uttaksperioder()).containsExactly(
            endringsmarkør,
            new UttaksPeriodeDto(cutoff.plusWeeks(1), cutoff.plusWeeks(1).plusDays(4), FELLESPERIODE, null, null, null, false, null, null));
    }

    private static UttakPeriodeDto periode(int offset, UttakDto søker, UttakDto annenPart, EøsUttakDto annenPartEøs) {
        var dato = START.plusWeeks(offset);
        return new UttakPeriodeDto(dato, dato.plusDays(4), søker, annenPart, annenPartEøs);
    }

    private static UttakDto utsettelse(UtsettelseÅrsak årsak) {
        return new UttakDto(MOR, null, årsak, null, null, ARBEID, null, false, null);
    }

    private static UttakPeriodeDto uttaksperiode(LocalDate fom, LocalDate tom, VedtattResultat resultat) {
        var uttak = new UttakDto(MOR, FELLESPERIODE, null, null, null, null, null, false, resultat);
        return new UttakPeriodeDto(fom, tom, uttak, null, null);
    }
}
