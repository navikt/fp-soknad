package no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.MorsAktivitet;
import no.nav.foreldrepenger.kontrakter.felles.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.Målform;
import no.nav.foreldrepenger.soknad.kontrakt.barn.FødselDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Aktivitet;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UtsettelsesPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UtsettelsesÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.Dokumenterer;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.InnsendingType;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.VedleggDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.ÅpenPeriodeDto;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Gradering;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Oppholdsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Overfoeringsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Person;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Utsettelsesperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Uttaksperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Virksomhet;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

class V3ForeldrepengerDomainMapperTest {
    private static final LocalDate FOM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TOM = FOM.plusDays(10);
    private static final V3ForeldrepengerDomainMapper MAPPER = new V3ForeldrepengerDomainMapper();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_søkerperiode_uten_konto_eller_utsettelse(boolean endring) {
        var plan = new UttaksplanDto(false, null, List.of(periode(uttak(null))));
        assertThatThrownBy(() -> map(plan, List.of(), endring))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Søkerperiode må ha konto eller utsettelsesårsak");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void avviser_gradering_uten_arbeidstidprosent(boolean endring) {
        var graderinger = List.of(new FellesUttaksplanDto.Gradering(null, null),
            new FellesUttaksplanDto.Gradering(new FellesUttaksplanDto.Arbeidstidprosent(null), null));
        for (var gradering : graderinger) {
            var søker = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FELLESPERIODE,
                null, null, gradering, null, null, false, null);
            var plan = new UttaksplanDto(false, null, List.of(periode(søker)));
            assertThatThrownBy(() -> map(plan, List.of(), endring))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gradering må ha arbeidstidprosent");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void ignorerer_ikke_null_elementer_i_ny_plan(boolean endring) {
        var plan = new UttaksplanDto(false, null, Collections.singletonList(null));
        assertThatThrownBy(() -> map(plan, List.of(), endring)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bruker_legacy_bare_når_nye_perioder_er_null(boolean endring) throws Exception {
        var xml = map(new UttaksplanDto(true, List.of(legacyUttak()), null), List.of(), endring);
        assertThat(xml.getPerioder()).singleElement().isInstanceOfSatisfying(Uttaksperiode.class,
            periode -> assertThat(periode.getType().getKode()).isEqualTo("FEDREKVOTE"));
        assertThat(xml.isOenskerJustertVedFoedsel()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tom_ny_liste_er_autoritativ(boolean endring) throws Exception {
        assertThat(map(new UttaksplanDto(false, List.of(legacyUttak()), List.of()), List.of(), endring).getPerioder()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sender_periode_der_bare_annen_part_har_uttak_som_opphold_og_overstyrer_legacy(boolean endring) throws Exception {
        var vedlegg = vedlegg(TOM.plusDays(1), TOM.plusDays(5), InnsendingType.LASTET_OPP);
        var xml = map(new UttaksplanDto(false, List.of(legacyUttak()), søkerAnnenPartOgEøs()), List.of(vedlegg), endring);
        assertThat(xml.getPerioder()).hasSize(2);
        assertThat(xml.getPerioder().getFirst()).isInstanceOfSatisfying(Uttaksperiode.class, periode -> {
            assertThat(periode.getType().getKode()).isEqualTo("FELLESPERIODE");
            assertThat(periode.getFom()).isEqualTo(FOM);
            assertThat(periode.getTom()).isEqualTo(TOM);
        });
        assertThat(xml.getPerioder().get(1)).isInstanceOfSatisfying(Oppholdsperiode.class, opphold -> {
            assertThat(opphold.getAarsak().getKode()).isEqualTo("UTTAK_FEDREKVOTE_ANNEN_FORELDER");
            assertThat(opphold.getFom()).isEqualTo(TOM.plusDays(1));
            assertThat(opphold.getTom()).isEqualTo(TOM.plusDays(5));
            assertThat(opphold.getVedlegg()).singleElement().satisfies(referanse ->
                assertThat(referanse.getValue()).isInstanceOfSatisfying(Vedlegg.class,
                    dokument -> assertThat(dokument.getId()).isEqualTo("V" + vedlegg.uuid())));
        });
    }

    @Test
    void fars_førstegangssøknad_sender_mors_kvote_som_opphold_før_eget_uttak() throws Exception {
        var perioder = List.of(new UttakPeriodeDto(FOM, TOM, null, uttak(KontoType.MØDREKVOTE), null),
            new UttakPeriodeDto(TOM.plusDays(1), TOM.plusDays(5), uttak(KontoType.FEDREKVOTE), null, null));
        var xml = map(new UttaksplanDto(false, null, perioder), List.of(), false);
        assertThat(xml.getPerioder()).hasSize(2);
        assertThat(xml.getPerioder().getFirst()).isInstanceOfSatisfying(Oppholdsperiode.class, opphold -> {
            assertThat(opphold.getAarsak().getKode()).isEqualTo("UTTAK_MØDREKVOTE_ANNEN_FORELDER");
            assertThat(opphold.getFom()).isEqualTo(FOM);
            assertThat(opphold.getTom()).isEqualTo(TOM);
        });
        assertThat(xml.getPerioder().get(1)).isInstanceOfSatisfying(Uttaksperiode.class, periode -> {
            assertThat(periode.getType().getKode()).isEqualTo("FEDREKVOTE");
            assertThat(periode.getFom()).isEqualTo(TOM.plusDays(1));
        });
    }

    @ParameterizedTest
    @CsvSource({
        "MØDREKVOTE, UTTAK_MØDREKVOTE_ANNEN_FORELDER", "FEDREKVOTE, UTTAK_FEDREKVOTE_ANNEN_FORELDER",
        "FELLESPERIODE, UTTAK_FELLESP_ANNEN_FORELDER", "FORELDREPENGER, UTTAK_FORELDREPENGER_ANNEN_FORELDER"
    })
    void periode_med_bare_annen_parts_uttak_sendes_som_opphold_i_begge_søknadstyper(KontoType konto, String årsak) throws Exception {
        var plan = new UttaksplanDto(false, null, List.of(new UttakPeriodeDto(FOM, TOM, null, uttak(konto), null)));
        for (var endring : List.of(false, true)) {
            assertThat(map(plan, List.of(), endring).getPerioder()).singleElement().isInstanceOfSatisfying(Oppholdsperiode.class,
                opphold -> assertThat(opphold.getAarsak().getKode()).isEqualTo(årsak));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sender_ikke_opphold_for_annen_parts_utsettelse_eller_foreldrepenger_før_fødsel(boolean endring) throws Exception {
        var utsettelse = new UttakDto(FellesUttaksplanDto.Rolle.FAR_MEDMOR, null, FellesUttaksplanDto.UtsettelseÅrsak.ARBEID,
            null, null, null, null, false, null);
        var perioder = List.of(new UttakPeriodeDto(FOM, TOM, null, uttak(KontoType.FORELDREPENGER_FØR_FØDSEL), null),
            new UttakPeriodeDto(TOM.plusDays(1), TOM.plusDays(5), null, utsettelse, null),
            new UttakPeriodeDto(TOM.plusDays(6), TOM.plusDays(10), uttak(KontoType.FELLESPERIODE), null, null));
        var xml = map(new UttaksplanDto(false, null, perioder), List.of(), endring);
        assertThat(xml.getPerioder()).singleElement().isInstanceOf(Uttaksperiode.class);
    }

    private static List<UttakPeriodeDto> søkerAnnenPartOgEøs() {
        var annenPart = uttak(KontoType.FEDREKVOTE);
        return List.of(new UttakPeriodeDto(FOM, TOM, uttak(KontoType.FELLESPERIODE), annenPart, null),
            new UttakPeriodeDto(TOM.plusDays(1), TOM.plusDays(5), null, annenPart, null),
            new UttakPeriodeDto(TOM.plusDays(6), TOM.plusDays(10), null, null,
                new FellesUttaksplanDto.EøsUttakDto(KontoType.FEDREKVOTE, new FellesUttaksplanDto.EøsUttakDto.Trekkdager(BigDecimal.ONE))));
    }

    @ParameterizedTest
    @CsvSource({
        "ARBEID, ARBEID", "FERIE, LOVBESTEMT_FERIE", "SØKER_SYKDOM, SYKDOM",
        "SØKER_INNLAGT, INSTITUSJONSOPPHOLD_SØKER", "BARN_INNLAGT, INSTITUSJONSOPPHOLD_BARNET",
        "HV_ØVELSE, HV_OVELSE", "NAV_TILTAK, NAV_TILTAK", "FRI, FRI"
    })
    void videresender_alle_utsettelser_i_begge_søknadstyper(FellesUttaksplanDto.UtsettelseÅrsak årsak, String kode) throws Exception {
        var søker = new UttakDto(FellesUttaksplanDto.Rolle.MOR, null, årsak, null, null, MorsAktivitet.ARBEID, null, false, null);
        var plan = new UttaksplanDto(false, null, List.of(periode(søker)));
        for (var endring : List.of(false, true)) {
            assertThat(map(plan, List.of(), endring).getPerioder()).singleElement().isInstanceOfSatisfying(Utsettelsesperiode.class, xml -> {
                assertThat(xml.getAarsak().getKode()).isEqualTo(kode);
                assertThat(xml.getMorsAktivitetIPerioden().getKode()).isEqualTo("ARBEID");
                assertThat(xml.getFom()).isEqualTo(FOM);
                assertThat(xml.getTom()).isEqualTo(TOM);
            });
        }
    }

    @ParameterizedTest
    @EnumSource(FellesUttaksplanDto.OverføringÅrsak.class)
    void mapper_overføring_direkte(FellesUttaksplanDto.OverføringÅrsak årsak) throws Exception {
        var søker = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FEDREKVOTE, null, årsak, null, null, null, false, null);
        for (var endring : List.of(false, true)) {
            var xml = map(new UttaksplanDto(false, null, List.of(periode(søker))), List.of(), endring);
            assertThat(xml.getPerioder()).singleElement().isInstanceOfSatisfying(Overfoeringsperiode.class, overføring -> {
                assertThat(overføring.getAarsak().getKode()).isEqualTo(årsak.name());
                assertThat(overføring.getOverfoeringAv().getKode()).isEqualTo("FEDREKVOTE");
            });
        }
    }

    @ParameterizedTest
    @EnumSource(Aktivitet.AktivitetType.class)
    void mapper_gradering_aktivitet_samtidiguttak_og_vedlegg(Aktivitet.AktivitetType type) throws Exception {
        var aktivitet = new Aktivitet(type,
            new FellesUttaksplanDto.Arbeidsgiver("999999999", FellesUttaksplanDto.Arbeidsgiver.ArbeidsgiverType.ORGANISASJON), null);
        var gradering = new FellesUttaksplanDto.Gradering(new FellesUttaksplanDto.Arbeidstidprosent(new BigDecimal("40.5")), aktivitet);
        var søker = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FELLESPERIODE, null, null, gradering, MorsAktivitet.ARBEID,
            new FellesUttaksplanDto.SamtidigUttak(new BigDecimal("59.5")), true, null);
        var vedlegg = vedlegg(FOM, TOM, InnsendingType.LASTET_OPP);
        for (var endring : List.of(false, true)) {
            var xml = map(new UttaksplanDto(false, null, List.of(periode(søker))),
                List.of(vedlegg, vedlegg(FOM.plusDays(1), TOM, InnsendingType.LASTET_OPP), vedlegg(FOM, TOM, InnsendingType.AUTOMATISK)), endring);
            assertThat(xml.getPerioder()).singleElement().isInstanceOfSatisfying(Gradering.class, gradert -> {
                assertThat(gradert.getArbeidtidProsent()).isEqualTo(40.5);
                assertThat(gradert.isArbeidsforholdSomSkalGraderes()).isTrue();
                assertThat(gradert.isErArbeidstaker()).isEqualTo(type == Aktivitet.AktivitetType.ORDINÆRT_ARBEID);
                assertThat(gradert.isErFrilanser()).isEqualTo(type == Aktivitet.AktivitetType.FRILANS);
                assertThat(gradert.isErSelvstNæringsdrivende()).isEqualTo(type == Aktivitet.AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE);
                assertThat(gradert.getArbeidsgiver()).isInstanceOfSatisfying(Virksomhet.class,
                    arbeidsgiver -> assertThat(arbeidsgiver.getIdentifikator()).isEqualTo("999999999"));
                assertThat(gradert.isOenskerSamtidigUttak()).isTrue();
                assertThat(gradert.getSamtidigUttakProsent()).isEqualTo(59.5);
                assertThat(gradert.isOenskerFlerbarnsdager()).isTrue();
                assertThat(gradert.getMorsAktivitetIPerioden().getKode()).isEqualTo("ARBEID");
                assertThat(gradert.getVedlegg()).singleElement().satisfies(referanse ->
                    assertThat(referanse.getValue()).isInstanceOfSatisfying(Vedlegg.class,
                        dokument -> assertThat(dokument.getId()).isEqualTo("V" + vedlegg.uuid())));
            });
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mapper_ugradert_samtidiguttak_og_vedlegg_på_alle_periodetyper(boolean endring) throws Exception {
        var samtidig = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FELLESPERIODE, null, null, null, MorsAktivitet.ARBEID,
            new FellesUttaksplanDto.SamtidigUttak(new BigDecimal("50")), true, null);
        var fri = new UttakDto(FellesUttaksplanDto.Rolle.MOR, null, FellesUttaksplanDto.UtsettelseÅrsak.FRI,
            null, null, null, null, false, null);
        var overføring = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FEDREKVOTE, null,
            FellesUttaksplanDto.OverføringÅrsak.ALENEOMSORG, null, null, null, false, null);
        var vedlegg = vedlegg(FOM, TOM, InnsendingType.LASTET_OPP);
        var plan = new UttaksplanDto(false, null, List.of(periode(samtidig), periode(fri), periode(overføring)));
        var xml = map(plan, List.of(vedlegg), endring);
        assertThat(xml.getPerioder()).hasSize(3).allSatisfy(periode ->
            assertThat(periode.getVedlegg()).singleElement().satisfies(referanse ->
                assertThat(referanse.getValue()).isInstanceOfSatisfying(Vedlegg.class,
                    dokument -> assertThat(dokument.getId()).isEqualTo("V" + vedlegg.uuid()))));
        assertThat(xml.getPerioder().getFirst()).isExactlyInstanceOf(Uttaksperiode.class);
        var uttak = (Uttaksperiode) xml.getPerioder().getFirst();
        assertThat(uttak.isOenskerSamtidigUttak()).isTrue();
        assertThat(uttak.getSamtidigUttakProsent()).isEqualTo(50.0);
        assertThat(uttak.isOenskerFlerbarnsdager()).isTrue();
        assertThat(uttak.getMorsAktivitetIPerioden().getKode()).isEqualTo("ARBEID");
        assertThat(xml.getPerioder().get(1)).isInstanceOfSatisfying(Utsettelsesperiode.class,
            utsettelse -> assertThat(utsettelse.getMorsAktivitetIPerioden()).isNull());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mapper_privat_arbeidsgiver_og_utelater_samtidiguttak_når_det_ikke_er_oppgitt(boolean endring) throws Exception {
        var aktivitet = new Aktivitet(Aktivitet.AktivitetType.ORDINÆRT_ARBEID,
            new FellesUttaksplanDto.Arbeidsgiver("99999999999", FellesUttaksplanDto.Arbeidsgiver.ArbeidsgiverType.PRIVAT), null);
        var gradering = new FellesUttaksplanDto.Gradering(new FellesUttaksplanDto.Arbeidstidprosent(BigDecimal.TEN), aktivitet);
        var søker = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FELLESPERIODE, null, null, gradering, null, null, false, null);
        var xml = map(new UttaksplanDto(false, null, List.of(periode(søker))), List.of(), endring);
        assertThat(xml.getPerioder()).singleElement().isInstanceOfSatisfying(Gradering.class, gradert -> {
            assertThat(gradert.getArbeidsgiver()).isInstanceOfSatisfying(Person.class,
                arbeidsgiver -> assertThat(arbeidsgiver.getIdentifikator()).isEqualTo("99999999999"));
            assertThat(gradert.isOenskerSamtidigUttak()).isNull();
            assertThat(gradert.getSamtidigUttakProsent()).isNull();
            assertThat(gradert.getMorsAktivitetIPerioden().getKode()).isEqualTo("-");
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void gradering_uten_aktivitet_har_ingen_arbeidsgiver_eller_aktivitetstype(boolean endring) throws Exception {
        var gradering = new FellesUttaksplanDto.Gradering(new FellesUttaksplanDto.Arbeidstidprosent(BigDecimal.TEN), null);
        var søker = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FELLESPERIODE, null, null, gradering, null, null, false, null);
        var xml = map(new UttaksplanDto(false, null, List.of(periode(søker))), List.of(), endring);
        assertThat(xml.getPerioder()).singleElement().isInstanceOfSatisfying(Gradering.class, gradert -> {
            assertThat(gradert.getArbeidsgiver()).isNull();
            assertThat(gradert.isErArbeidstaker()).isFalse();
            assertThat(gradert.isErFrilanser()).isFalse();
            assertThat(gradert.isErSelvstNæringsdrivende()).isFalse();
        });
    }

    @ParameterizedTest
    @MethodSource("legacyPlaner")
    void endring_viderefører_historikk_og_avslag_uavhengig_av_legacy(List<Uttaksplanperiode> legacy) throws Exception {
        var avslag = new FellesUttaksplanDto.VedtattResultat(false, false, false, FellesUttaksplanDto.VedtattResultat.Årsak.ANNET);
        var innvilget = new FellesUttaksplanDto.VedtattResultat(true, false, true, FellesUttaksplanDto.VedtattResultat.Årsak.ANNET);
        var avslåttUttak = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.FELLESPERIODE, null, null, null, null, null, false, avslag);
        var historiskUttak = new UttakDto(FellesUttaksplanDto.Rolle.MOR, KontoType.MØDREKVOTE, null, null, null, null, null, false, innvilget);
        var perioder = List.of(new UttakPeriodeDto(FOM.minusYears(1), TOM.minusYears(1), historiskUttak, null, null),
            periode(avslåttUttak), new UttakPeriodeDto(FOM.plusMonths(3), TOM.plusMonths(3), uttak(KontoType.FELLESPERIODE), null, null));
        var xml = map(new UttaksplanDto(false, legacy, perioder), List.of(), true);
        assertThat(xml.getPerioder()).hasSize(3).allSatisfy(periode -> assertThat(periode).isExactlyInstanceOf(Uttaksperiode.class));
        assertThat(xml.getPerioder()).extracting(periode -> periode.getFom()).containsExactly(FOM.minusYears(1), FOM, FOM.plusMonths(3));
    }

    private static Stream<List<Uttaksplanperiode>> legacyPlaner() {
        return Stream.of(null, List.of(), List.of(legacyUttak()),
            List.of(new UtsettelsesPeriodeDto(FOM.plusMonths(2), TOM.plusMonths(2), UtsettelsesÅrsak.FRI, null, false)));
    }

    private static UttaksPeriodeDto legacyUttak() {
        return new UttaksPeriodeDto(FOM.plusMonths(1), TOM.plusMonths(1), KontoType.FEDREKVOTE, null, false, null, false, false, null);
    }

    private static UttakDto uttak(KontoType konto) {
        return new UttakDto(FellesUttaksplanDto.Rolle.MOR, konto, null, null, null, null, null, false, null);
    }

    private static UttakPeriodeDto periode(UttakDto søker) {
        return new UttakPeriodeDto(FOM, TOM, søker, null, null);
    }

    private static VedleggDto vedlegg(LocalDate fom, LocalDate tom, InnsendingType type) {
        return new VedleggDto(UUID.randomUUID(), DokumentTypeId.I000023, type, null,
            new Dokumenterer(Dokumenterer.DokumentererType.UTTAK, null, List.of(new ÅpenPeriodeDto(fom, tom))));
    }

    private static Fordeling map(UttaksplanDto plan, List<VedleggDto> vedlegg, boolean endring) throws Exception {
        var mottatt = LocalDateTime.of(2026, 9, 30, 12, 0);
        var barn = new FødselDto(1, FOM, FOM);
        var aktør = new AktørId("1234567890123");
        String xml;
        if (endring) {
            xml = MAPPER.tilXML(new EndringssøknadForeldrepengerDto(mottatt, new Saksnummer("123456789"), null,
                BrukerRolle.MOR, Målform.NB, barn, null, plan, vedlegg), mottatt, aktør);
        } else {
            xml = MAPPER.tilXML(new ForeldrepengesøknadDto(mottatt, null, BrukerRolle.MOR, Målform.NB, barn,
                null, null, List.of(), null, Dekningsgrad.HUNDRE, plan, List.of(), vedlegg), mottatt, aktør);
        }
        var context = JAXBContext.newInstance(Soeknad.class, Foreldrepenger.class, Endringssoeknad.class,
            no.nav.vedtak.felles.xml.soeknad.v3.ObjectFactory.class,
            no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.ObjectFactory.class,
            no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.ObjectFactory.class);
        var søknad = (Soeknad) ((JAXBElement<?>) context.createUnmarshaller().unmarshal(new StringReader(xml))).getValue();
        var ytelse = ((JAXBElement<?>) søknad.getOmYtelse().getAny().getFirst()).getValue();
        return endring ? ((Endringssoeknad) ytelse).getFordeling() : ((Foreldrepenger) ytelse).getFordeling();
    }
}
