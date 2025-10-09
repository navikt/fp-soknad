package no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper;

import static com.neovisionaries.i18n.CountryCode.NO;
import static com.neovisionaries.i18n.CountryCode.XK;
import static java.time.LocalDate.now;
import static java.time.Month.OCTOBER;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static no.nav.foreldrepenger.common.domain.felles.InnsendingsType.LASTET_OPP;
import static no.nav.foreldrepenger.common.domain.felles.InnsendingsType.SEND_SENERE;
import static no.nav.foreldrepenger.common.util.StreamUtil.safeStream;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.DokumentasjonReferanseMapper.dokumentasjonSomDokumentererOpptjeningsperiode;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.neovisionaries.i18n.CountryCode;

import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.domain.felles.InnsendingsType;
import no.nav.foreldrepenger.common.domain.felles.opptjening.AnnenOpptjeningType;
import no.nav.foreldrepenger.common.domain.felles.opptjening.Virksomhetstype;
import no.nav.foreldrepenger.common.error.UnexpectedInputException;
import no.nav.foreldrepenger.common.oppslag.dkif.Målform;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AdopsjonDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AnnenInntektDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.BarnDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.FrilansDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.FødselDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.NæringDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.OmsorgsovertakelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.TerminDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggInnsendingType;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ÅpenPeriodeDto;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Medlemskap;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.OppholdUtlandet;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Periode;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.AnnenOpptjening;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.EgenNaering;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Frilans;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.NorskOrganisasjon;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Opptjening;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.UtenlandskArbeidsforhold;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.UtenlandskOrganisasjon;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.AnnenOpptjeningTyper;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Brukerroller;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Innsendingstype;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Land;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Spraakkode;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Virksomhetstyper;

final class V3DomainMapperCommon {
    private static final Land KOSOVO = landFra("XXK");
    private static final no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.ObjectFactory FP_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.ObjectFactory();

    private V3DomainMapperCommon() {

    }

    static LocalDate relasjonDato(BarnDto barn) {
        return switch (barn) {
            case AdopsjonDto a -> a.adopsjonsdato();
            case FødselDto f -> f.fødselsdato();
            case OmsorgsovertakelseDto o -> o.foreldreansvarsdato();
            case TerminDto t -> t.termindato();
        };
    }

    static Spraakkode målformFra(Målform målform) {
        var spraakkkode = new Spraakkode();
        spraakkkode.setKode(målform.name());
        return spraakkkode;
    }

    static Opptjening opptjeningFra(NæringDto egenNæring, FrilansDto frilans, List<AnnenInntektDto> annenOpptjening, List<VedleggDto> vedlegg) {
        var opptjeningXml = new Opptjening();
        opptjeningXml.getUtenlandskArbeidsforhold().addAll(utenlandskeArbeidsforholdFra(annenOpptjening, vedlegg));
        opptjeningXml.setFrilans(frilansFra(frilans));
        egenNæringFra(egenNæring, vedlegg).ifPresent(n -> opptjeningXml.getEgenNaering().add(n));
        opptjeningXml.getAnnenOpptjening().addAll(andreOpptjeningerFra(annenOpptjening, vedlegg));
        return opptjeningXml;
    }

    static Medlemskap medlemsskapFra(List<UtenlandsoppholdsperiodeDto> opphold, LocalDate relasjonsDato) {
        var medlemskap = new Medlemskap();
        medlemskap.getOppholdUtlandet().addAll(oppholdUtlandetFra(opphold));
        medlemskap.setINorgeVedFoedselstidspunkt(varINorge(opphold, relasjonsDato));
        return medlemskap;
    }

    public static boolean varINorge(List<UtenlandsoppholdsperiodeDto> opphold, LocalDate dato) {
        return NO.equals(landVedDato(opphold, dato));
    }

    private static CountryCode landVedDato(List<UtenlandsoppholdsperiodeDto> utenlandsopphold, LocalDate dato) {
        return safeStream(utenlandsopphold)
            .filter(s -> dato.isAfter(s.fom().minusDays(1)) && dato.isBefore(s.tom().plusDays(1)))
            .map(UtenlandsoppholdsperiodeDto::landkode)
            .findFirst()
            .orElse(NO);
    }

    private static List<OppholdUtlandet> oppholdUtlandetFra(List<UtenlandsoppholdsperiodeDto> utenlandsopphold) {
        return safeStream(utenlandsopphold)
                .map(V3DomainMapperCommon::tilOppholdUtlandet)
                .toList();
    }

    private static OppholdUtlandet tilOppholdUtlandet(UtenlandsoppholdsperiodeDto o) {
        var oppholdUtlandet = new OppholdUtlandet();
        oppholdUtlandet.setPeriode(tilPeriode(o.fom(), o.tom()));
        oppholdUtlandet.setLand(landFra(o.landkode()));
        return oppholdUtlandet;
    }

    private static Periode tilPeriode(LocalDate fom) {
        var periode = new Periode();
        periode.setFom(fom);
        return periode;
    }

    private static Periode tilPeriode(LocalDate fom, LocalDate tom) {
        var periode = new Periode();
        periode.setFom(fom);
        periode.setTom(tom);
        return periode;
    }

    private static Virksomhetstyper virksomhetsTypeFra(Virksomhetstype type) {
        return Optional.ofNullable(type)
                .map(Virksomhetstype::name)
                .map(V3DomainMapperCommon::virksomhetsTypeFra)
                .orElse(null);
    }

    private static Virksomhetstyper virksomhetsTypeFra(String type) {
        var virksomhetstyper = new Virksomhetstyper();
        virksomhetstyper.setKode(type);
        virksomhetstyper.setKodeverk(virksomhetstyper.getKodeverk()); // bruker default fra getter..
        return virksomhetstyper;
    }

    private static Optional<EgenNaering> egenNæringFra(NæringDto egenNæring, List<VedleggDto> vedlegg) {
        return Optional.ofNullable(egenNæring).map(n -> create(n, vedlegg));
    }

    private static EgenNaering create(NæringDto egenNæring, List<VedleggDto> vedlegg) {
        // Fiskere kan svare nei på om den er registert i norge og deretter velge norge for å unngå å fylle inn orgnummer
        // I dette tilfelle vil de bli lagret som utenlandsk næring
        var vedleggReferanser = dokumentasjonSomDokumentererOpptjeningsperiode(vedlegg, new ÅpenPeriodeDto(egenNæring.fom(), egenNæring.tom()));
        if (CountryCode.NO.equals(egenNæring.registrertILand()) && egenNæring.organisasjonsnummer() != null) {
            return norskOrganisasjon(egenNæring, vedleggReferanser);
        } else {
            return utenlandskOrganisasjon(egenNæring, vedleggReferanser);
        }
    }

    private static UtenlandskOrganisasjon utenlandskOrganisasjon(NæringDto utenlandskOrg, List<UUID> vedleggReferanser) {
        var utenlandskOrganisasjon = new UtenlandskOrganisasjon();
        utenlandskOrganisasjon.getVedlegg().addAll(egenNæringVedleggFraIDs(vedleggReferanser));
        utenlandskOrganisasjon.setBeskrivelseAvEndring(utenlandskOrg.varigEndringBeskrivelse());
        utenlandskOrganisasjon.setNaerRelasjon(false); // Hardkodet
        utenlandskOrganisasjon.setEndringsDato(utenlandskOrg.varigEndringDato());
        utenlandskOrganisasjon.setOppstartsdato(utenlandskOrg.oppstartsdato());
        utenlandskOrganisasjon.setErNyoppstartet(erNyopprettet(utenlandskOrg.fom()));
        utenlandskOrganisasjon.setErNyIArbeidslivet(utenlandskOrg.harBlittYrkesaktivILøpetAvDeTreSisteFerdigliknedeÅrene());
        utenlandskOrganisasjon.setErVarigEndring(utenlandskOrg.hattVarigEndringAvNæringsinntektSiste4Kalenderår());
        utenlandskOrganisasjon.setNaeringsinntektBrutto(BigInteger.valueOf(utenlandskOrg.næringsinntekt()));
        utenlandskOrganisasjon.setNavn(utenlandskOrg.navnPåNæringen());
        utenlandskOrganisasjon.setRegistrertILand(landFra(utenlandskOrg.registrertILand()));
        utenlandskOrganisasjon.setPeriode(tilPeriode(utenlandskOrg.fom(), utenlandskOrg.tom()));
        utenlandskOrganisasjon.getVirksomhetstype().add(virksomhetsTypeFra(utenlandskOrg.næringstype()));
        return utenlandskOrganisasjon;
    }



    private static NorskOrganisasjon norskOrganisasjon(NæringDto norskOrg, List<UUID> vedleggReferanser) {
        var norskOrganisasjon = new NorskOrganisasjon();
        norskOrganisasjon.getVedlegg().addAll(egenNæringVedleggFraIDs(vedleggReferanser));
        norskOrganisasjon.setBeskrivelseAvEndring(norskOrg.varigEndringBeskrivelse());
        norskOrganisasjon.setNaerRelasjon(false); // Hardkodet
        norskOrganisasjon.setEndringsDato(norskOrg.varigEndringDato());
        norskOrganisasjon.setOppstartsdato(norskOrg.oppstartsdato());
        norskOrganisasjon.setErNyoppstartet(erNyopprettet(norskOrg.fom()));
        norskOrganisasjon.setErNyIArbeidslivet(norskOrg.harBlittYrkesaktivILøpetAvDeTreSisteFerdigliknedeÅrene());
        norskOrganisasjon.setErVarigEndring(norskOrg.hattVarigEndringAvNæringsinntektSiste4Kalenderår());
        norskOrganisasjon.setNaeringsinntektBrutto(BigInteger.valueOf(norskOrg.næringsinntekt()));
        norskOrganisasjon.setNavn(norskOrg.navnPåNæringen());
        norskOrganisasjon.setOrganisasjonsnummer(norskOrg.organisasjonsnummer().value());
        norskOrganisasjon.setPeriode(tilPeriode(norskOrg.fom(), norskOrg.tom()));
        norskOrganisasjon.getVirksomhetstype().add(virksomhetsTypeFra(norskOrg.næringstype()));
        return norskOrganisasjon;
    }

    private static List<JAXBElement<Object>> egenNæringVedleggFraIDs(List<UUID> vedlegg) {
        return safeStream(vedlegg)
                .filter(Objects::nonNull)
                .map(referanse -> FP_FACTORY_V3.createEgenNaeringVedlegg(tilVedlegg(referanse)))
                .toList();
    }

    static Vedlegg tilVedlegg(UUID referanse) {
        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setId(tilGyldigVedleggreferanse(referanse));
        return vedlegg;
    }

    // The @XmlID annotation requires the value to be a valid XML ID, which must start with a letter or underscore and cannot start with a digit.
    // UUID may contain a digit at the start, so we prefix with 'V' to ensure it's a valid XML ID.
    private static String tilGyldigVedleggreferanse(UUID uuid) {
        return "V" + uuid;
    }

    private static List<AnnenOpptjening> andreOpptjeningerFra(List<AnnenInntektDto> annenOpptjening, List<VedleggDto> vedlegg) {
        return safeStream(annenOpptjening)
            .filter(a -> !AnnenOpptjeningType.JOBB_I_UTLANDET.equals(a.type()))
            .map((AnnenInntektDto annen) -> annenOpptjeningFra(annen, vedlegg))
            .toList();
    }

    private static AnnenOpptjening annenOpptjeningFra(AnnenInntektDto annenInntektDto, List<VedleggDto> vedlegg) {
        return Optional.ofNullable(annenInntektDto)
                .map(annen -> create(annen, DokumentasjonReferanseMapper.dokumentasjonSomDokumentererOpptjeningsperiode(vedlegg,
                    new ÅpenPeriodeDto(annen.fom(), annen.tom()))))
                .orElse(null);
    }

    private static AnnenOpptjeningTyper create(String kode) {
        var type = new AnnenOpptjeningTyper();
        type.setKode(kode);
        type.setKodeverk(type.getKodeverk()); // bruker default fra getter..
        return type;
    }

    private static AnnenOpptjening create(AnnenInntektDto annen, List<UUID> vedleggreferanser) {
        var annenOpptjening = new AnnenOpptjening();
        annenOpptjening.getVedlegg().addAll(annenOpptjeningVedleggFra(vedleggreferanser));
        annenOpptjening.setType(annenOpptjeningTypeFra(annen.type()));
        annenOpptjening.setPeriode(tilPeriode(annen.fom(), annen.tom()));
        return annenOpptjening;
    }

    private static AnnenOpptjeningTyper annenOpptjeningTypeFra(AnnenOpptjeningType type) {
        return Optional.ofNullable(type)
                .map(AnnenOpptjeningType::name)
                .map(V3DomainMapperCommon::create)
                .orElse(null);
    }

    static Land landFra(CountryCode land) {
        if (XK.equals(land)) {
            return KOSOVO; // https://jira.adeo.no/browse/PFP-6077
        }
        return Optional.ofNullable(land)
                .map(s -> landFra(s.getAlpha3()))
                .orElse(null);
    }

    private static List<UtenlandskArbeidsforhold> utenlandskeArbeidsforholdFra(List<AnnenInntektDto> arbeidsforhold, List<VedleggDto> vedlegg) {
        return safeStream(arbeidsforhold)
                .filter(u -> AnnenOpptjeningType.JOBB_I_UTLANDET.equals(u.type()))
                .map(anneninntekt -> utenlandskArbeidsforholdFra(anneninntekt, dokumentasjonSomDokumentererOpptjeningsperiode(vedlegg, new ÅpenPeriodeDto(anneninntekt.fom(), anneninntekt.tom()))))
                .toList();
    }

    private static UtenlandskArbeidsforhold utenlandskArbeidsforholdFra(AnnenInntektDto anneninntekt, List<UUID> vedleggreferanser) {
        var utenlandskArbeidsforhold = new UtenlandskArbeidsforhold();
        utenlandskArbeidsforhold.getVedlegg().addAll(utenlandsArbeidsforholdVedleggFra(vedleggreferanser));
        utenlandskArbeidsforhold.setArbeidsgiversnavn(anneninntekt.arbeidsgiverNavn());
        utenlandskArbeidsforhold.setArbeidsland(landFra(anneninntekt.land()));
        utenlandskArbeidsforhold.setPeriode(tilPeriode(anneninntekt.fom(), anneninntekt.tom()));
        return utenlandskArbeidsforhold;
    }

    private static List<JAXBElement<Object>> utenlandsArbeidsforholdVedleggFra(List<UUID> vedlegg) {
        return safeStream(vedlegg)
                .filter(Objects::nonNull)
                .map(referanse -> FP_FACTORY_V3.createUtenlandskArbeidsforholdVedlegg(tilVedlegg(referanse)))
                .toList();
    }

    static Bruker søkerFra(AktørId aktørId, BrukerRolle rolle) {
        var bruker = new Bruker();
        bruker.setAktoerId(aktørId.value());
        bruker.setSoeknadsrolle(brukerRolleFra(BrukerRolle.MOR)); // TODO: Hardkodet til mor for alle ES søknader. Har vært slikt alltid
        return bruker;
    }

    static List<Vedlegg> vedleggFra(List<VedleggDto> vedlegg) {
        return safeStream(vedlegg)
            .filter(v -> !VedleggInnsendingType.AUTOMATISK.equals(v.innsendingsType())) // Skal ikke sende med automatiske vedlegg til journalføring
            .map(V3DomainMapperCommon::vedleggFra)
            .toList();
    }

    private static Vedlegg vedleggFra(VedleggDto vedlegg) {
        var vedleggXML = new Vedlegg();
        vedleggXML.setId(tilGyldigVedleggreferanse(vedlegg.uuid()));
        vedleggXML.setTilleggsinformasjon(vedlegg.beskrivelse());
        vedleggXML.setSkjemanummer(vedlegg.skjemanummer().getKode());
        vedleggXML.setInnsendingstype(innsendingstypeFra(vedlegg.innsendingsType()));
        return vedleggXML;
    }

    private static Innsendingstype innsendingstypeFra(VedleggInnsendingType innsendingsType) {
        return switch (innsendingsType) {
            case SEND_SENERE -> innsendingsTypeMedKodeverk(SEND_SENERE);
            case LASTET_OPP -> innsendingsTypeMedKodeverk(LASTET_OPP);
            case AUTOMATISK -> throw new UnexpectedInputException("Automatiske vedlegg skal ikke sendes til journalføring");
        };
    }

    private static Frilans frilansFra(FrilansDto frilans) {
        return Optional.ofNullable(frilans)
                .map(V3DomainMapperCommon::create)
                .orElse(null);
    }

    private static Frilans create(FrilansDto frilans) {
        var frilansXML = new Frilans();
        frilansXML.getPeriode().add(tilPeriode(frilans.oppstart()));
        frilansXML.setHarInntektFraFosterhjem(false);
        frilansXML.setNaerRelasjon(false);
        frilansXML.setErNyoppstartet(frilans.oppstart().isAfter(now().minusMonths(3)));
        return frilansXML;
    }

    private static List<JAXBElement<Object>> annenOpptjeningVedleggFra(List<UUID> vedlegg) {
        return safeStream(vedlegg)
                .filter(Objects::nonNull)
                .map(referanse -> FP_FACTORY_V3.createAnnenOpptjeningVedlegg(tilVedlegg(referanse)))
                .toList();
    }



    private static Innsendingstype innsendingsTypeMedKodeverk(InnsendingsType type) {
        var innsendingstype = new Innsendingstype();
        innsendingstype.setKode(type.name());
        innsendingstype.setKodeverk(innsendingstype.getKodeverk()); // TODO: Fjern..
        return innsendingstype;
    }

    private static Brukerroller brukerRolleFra(BrukerRolle søknadsRolle) {
        return brukerRolleFra(søknadsRolle.name());
    }

    private static Brukerroller brukerRolleFra(String rolle) {
        var brukerRolle = new Brukerroller();
        brukerRolle.setKode(rolle);
        brukerRolle.setKodeverk(brukerRolle.getKodeverk()); // TODO: Fjern..
        return brukerRolle;
    }

    private static Land landFra(String alpha3) {
        var land = new Land();
        land.setKode(alpha3);
        land.setKodeverk(land.getKodeverk()); // TODO: Fjern..
        return land;
    }

    static boolean erNyopprettet(LocalDate fom) {
        return erNyopprettet(LocalDate.now(), fom);
    }

    static boolean erNyopprettet(LocalDate nå, LocalDate fom) {
        return fom.isAfter(now().minusYears(nå.isAfter(LocalDate.of(nå.getYear(), OCTOBER, 20)) ? 3 : 4).with(firstDayOfYear()).minusDays(1));
    }
    static boolean toBoolean(Boolean bool) {
        return bool != null && bool;
    }
}
