package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
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
        assertThat(EndringsdatoFilter.perioderFraEndringsdato(PLAN, ENDRINGSDATO)).containsExactly(PÅ_ENDRINGSDATO, ETTER);
    }

    @Test
    void uten_endringsdato_returneres_alle_perioder() {
        assertThat(EndringsdatoFilter.perioderFraEndringsdato(PLAN, null)).containsExactlyElementsOf(PLAN);
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
