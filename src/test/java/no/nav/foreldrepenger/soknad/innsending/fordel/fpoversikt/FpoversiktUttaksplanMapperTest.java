package no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.MorsAktivitet.ARBEID;
import static no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle.FAR_MEDMOR;
import static no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle.MOR;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Aktivitet;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Arbeidsgiver;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Arbeidstidprosent;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.EøsUttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Gradering;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.SamtidigUttak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.VedtattResultat;

class FpoversiktUttaksplanMapperTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 5);
    private static final LocalDateTime MOTTATT = LocalDateTime.of(2026, 1, 1, 12, 30);

    @Test
    void mapper_kun_norsk_annen_part_og_fjerner_resultat() {
        var arbeidsgiver = new Arbeidsgiver("999999999", Arbeidsgiver.ArbeidsgiverType.ORGANISASJON);
        var gradering = new Gradering(new Arbeidstidprosent(BigDecimal.valueOf(40)),
            new Aktivitet(Aktivitet.AktivitetType.ORDINÆRT_ARBEID, arbeidsgiver, "Arbeidsgiver"));
        var samtidigUttak = new SamtidigUttak(BigDecimal.valueOf(20));
        var resultat = new VedtattResultat(true, false, true, VedtattResultat.Årsak.ANNET);
        var annenPart = new UttakDto(FAR_MEDMOR, FELLESPERIODE, null, null, gradering, ARBEID, samtidigUttak, true, resultat);
        var søker = new UttakDto(MOR, FELLESPERIODE, null, null, null, null, null, false, resultat);
        var eøs = new EøsUttakDto(FELLESPERIODE, new EøsUttakDto.Trekkdager(BigDecimal.ONE));
        var plan = new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE, List.of(
            new UttakPeriodeDto(START, START.plusDays(4), søker, null, null),
            new UttakPeriodeDto(START.plusWeeks(1), START.plusWeeks(1).plusDays(4), null, annenPart, null),
            new UttakPeriodeDto(START.plusWeeks(2), START.plusWeeks(2).plusDays(4), null, null, eøs)));

        var request = FpoversiktUttaksplanMapper.map("123", MOTTATT, plan).orElseThrow();

        assertThat(request.saksnummer()).isEqualTo("123");
        assertThat(request.mottattTidspunkt()).isEqualTo(MOTTATT);
        assertThat(request.perioder()).singleElement().satisfies(periode -> {
            assertThat(periode.fom()).isEqualTo(START.plusWeeks(1));
            assertThat(periode.tom()).isEqualTo(START.plusWeeks(1).plusDays(4));
            assertThat(periode.uttak().forelder()).isEqualTo(annenPart.forelder());
            assertThat(periode.uttak().kontoType()).isEqualTo(annenPart.kontoType());
            assertThat(periode.uttak().gradering().arbeidstidprosent()).isEqualTo(gradering.arbeidstidprosent());
            assertThat(periode.uttak().gradering().aktivitet()).isNull();
            assertThat(periode.uttak().resultat()).isNull();
        });
    }

    @Test
    void felles_plan_uten_norsk_annen_part_gir_tom_erstatningsliste() {
        var søker = new UttakDto(MOR, FELLESPERIODE, null, null, null, null, null, false, null);
        var eøs = new EøsUttakDto(FELLESPERIODE, new EøsUttakDto.Trekkdager(BigDecimal.ONE));
        var plan = new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE,
            List.of(new UttakPeriodeDto(START, START.plusDays(4), søker, null, eøs)));

        assertThat(FpoversiktUttaksplanMapper.map("123", MOTTATT, plan))
            .get()
            .extracting(FpoversiktUttaksplanRequest::perioder)
            .asList()
            .isEmpty();
        assertThat(FpoversiktUttaksplanMapper.map("123", MOTTATT,
            new FellesUttaksplanDto(START, 1, FellesUttaksplanDto.Dekningsgrad.HUNDRE, List.of())))
            .get()
            .extracting(FpoversiktUttaksplanRequest::perioder)
            .asList()
            .isEmpty();
    }

    @Test
    void manglende_felles_plan_gir_ingen_request() {
        assertThat(FpoversiktUttaksplanMapper.map("123", MOTTATT, null)).isEmpty();
    }
}
