package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UtsettelseÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;

class EndringsdatoFilterTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 5);
    private static final UttakDto UTTAK = new UttakDto(Rolle.MOR, MØDREKVOTE, null, null, null, null, null, false, null);

    private static final UttakPeriodeDto FØR = new UttakPeriodeDto(START, START.plusDays(4), UTTAK, null, null);
    private static final UttakPeriodeDto ANNEN_PART_FØR = new UttakPeriodeDto(START.plusDays(7), START.plusDays(11), null, UTTAK, null);
    private static final UttakPeriodeDto PÅ_ENDRINGSDATO = new UttakPeriodeDto(START.plusDays(14), START.plusDays(18), UTTAK, null, null);
    private static final UttakPeriodeDto ETTER = new UttakPeriodeDto(START.plusDays(21), START.plusDays(25), null, UTTAK, null);
    private static final LocalDate ENDRINGSDATO = PÅ_ENDRINGSDATO.fom();
    private static final List<UttakPeriodeDto> PLAN = List.of(FØR, ANNEN_PART_FØR, PÅ_ENDRINGSDATO, ETTER);

    @Test
    void beholder_perioder_fra_og_med_endringsdato_for_begge_parter() {
        assertThat(EndringsdatoFilter.perioderFraEndringsdato(PLAN, ENDRINGSDATO, BrukerRolle.MOR)).containsExactly(PÅ_ENDRINGSDATO, ETTER);
    }

    @Test
    void søkerperiode_som_krysser_endringsdato_kuttes_og_beholder_øvrige_felter() {
        var medFlagg = new UttakDto(Rolle.MOR, MØDREKVOTE, null, null, null, null, null, true, null);
        var kryssende = new UttakPeriodeDto(START, START.plusDays(9), medFlagg, null, null);
        var endringsdato = START.plusDays(3);

        var perioder = EndringsdatoFilter.perioderFraEndringsdato(List.of(kryssende), endringsdato, BrukerRolle.MOR);

        assertThat(perioder).containsExactly(new UttakPeriodeDto(endringsdato, kryssende.tom(), medFlagg, null, null));
    }

    @Test
    void søkerperiode_som_slutter_på_endringsdato_kuttes_til_én_dag() {
        var kryssende = new UttakPeriodeDto(START, START.plusDays(3), UTTAK, null, null);
        var endringsdato = kryssende.tom();

        var perioder = EndringsdatoFilter.perioderFraEndringsdato(List.of(kryssende, ETTER), endringsdato, BrukerRolle.MOR);

        assertThat(perioder).containsExactly(new UttakPeriodeDto(endringsdato, endringsdato, UTTAK, null, null), ETTER);
    }

    @Test
    void annen_parts_periode_som_krysser_endringsdato_kuttes_og_søker_får_fri_utsettelse() {
        var endringsdato = ANNEN_PART_FØR.fom().plusDays(2);

        var perioder = EndringsdatoFilter.perioderFraEndringsdato(PLAN, endringsdato, BrukerRolle.MOR);

        assertThat(perioder).containsExactly(fri(Rolle.MOR, endringsdato, PÅ_ENDRINGSDATO.fom().minusDays(1)),
            new UttakPeriodeDto(endringsdato, ANNEN_PART_FØR.tom(), null, UTTAK, null), PÅ_ENDRINGSDATO, ETTER);
    }

    @Test
    void uten_endringsdato_returneres_alle_perioder() {
        assertThat(EndringsdatoFilter.perioderFraEndringsdato(PLAN, null, BrukerRolle.MOR)).containsExactlyElementsOf(PLAN);
    }

    @Test
    void utsatt_oppstart_gir_fri_utsettelse_fram_til_neste_søkerperiode() {
        var endringsdato = START.plusDays(10);

        var perioder = EndringsdatoFilter.perioderFraEndringsdato(PLAN, endringsdato, BrukerRolle.FAR);

        assertThat(perioder).containsExactly(fri(Rolle.FAR_MEDMOR, endringsdato, PÅ_ENDRINGSDATO.fom().minusDays(1)),
            new UttakPeriodeDto(endringsdato, ANNEN_PART_FØR.tom(), null, UTTAK, null), PÅ_ENDRINGSDATO, ETTER);
    }

    @Test
    void endringsdato_på_annen_parts_periode_gir_fri_utsettelse_for_søker() {
        var endringsdato = ANNEN_PART_FØR.fom();

        var perioder = EndringsdatoFilter.perioderFraEndringsdato(PLAN, endringsdato, BrukerRolle.MOR);

        assertThat(perioder).containsExactly(fri(Rolle.MOR, endringsdato, PÅ_ENDRINGSDATO.fom().minusDays(1)), ANNEN_PART_FØR,
            PÅ_ENDRINGSDATO, ETTER);
    }

    @Test
    void slettet_siste_periode_gir_én_dag_fri_utsettelse() {
        var endringsdato = START.plusDays(30);

        var perioder = EndringsdatoFilter.perioderFraEndringsdato(List.of(FØR), endringsdato, BrukerRolle.MOR);

        assertThat(perioder).containsExactly(fri(Rolle.MOR, endringsdato, endringsdato));
    }

    @Test
    void ingen_senere_søkerperiode_gir_én_dag_fri_utsettelse_foran_annen_parts_perioder() {
        var perioder = EndringsdatoFilter.perioderFraEndringsdato(PLAN, ETTER.fom(), BrukerRolle.MOR);

        assertThat(perioder).containsExactly(fri(Rolle.MOR, ETTER.fom(), ETTER.fom()), ETTER);
    }

    private static UttakPeriodeDto fri(Rolle rolle, LocalDate fom, LocalDate tom) {
        return new UttakPeriodeDto(fom, tom, new UttakDto(rolle, null, UtsettelseÅrsak.FRI, null, null, null, null, false, null), null, null);
    }

    @Test
    void filtrert_søknad_beholder_øvrige_felter_og_original_er_uendret() {
        var søknad = new EndringssøknadBuilder(new Saksnummer("123456")).medUttaksplan(PLAN).medEndringsdato(ENDRINGSDATO).build();

        var filtrert = EndringsdatoFilter.fraEndringsdato(søknad);

        assertThat(filtrert.uttaksplan().perioder()).containsExactly(PÅ_ENDRINGSDATO, ETTER);
        assertThat(filtrert).usingRecursiveComparison().ignoringFields("uttaksplan.perioder").isEqualTo(søknad);
        assertThat(søknad.uttaksplan().perioder()).containsExactlyElementsOf(PLAN);
    }

    @Test
    void søknad_uten_endringsdato_returneres_uendret() {
        var søknad = new EndringssøknadBuilder(new Saksnummer("123456")).medUttaksplan(PLAN).build();
        assertThat(EndringsdatoFilter.fraEndringsdato(søknad)).isSameAs(søknad);
        assertThat(EndringsdatoFilter.uttaksplanFraEndringsdato(søknad)).isSameAs(søknad.uttaksplan());
    }
}
