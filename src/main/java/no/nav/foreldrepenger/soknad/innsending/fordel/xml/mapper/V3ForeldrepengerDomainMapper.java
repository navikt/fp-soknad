package no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper;

import static no.nav.foreldrepenger.common.util.StreamUtil.safeStream;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.DokumentasjonReferanseMapper.dokumentasjonSomDokumentererBarn;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.DokumentasjonReferanseMapper.dokumentasjonSomDokumentererUttaksperiode;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.landFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.medlemsskapFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.målformFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.opptjeningFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.relasjonDato;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.søkerFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.tilVedlegg;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.toBooleanNullSafe;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.vedleggFra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.MorsAktivitet;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.Oppholdsårsak;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.Overføringsårsak;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.UtsettelsesÅrsak;
import no.nav.foreldrepenger.common.error.UnexpectedInputException;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.Personoppslag;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.jaxb.FPV3JAXBUtil;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AdopsjonDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.BarnDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.FødselDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.OmsorgsovertakelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.TerminDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.annenpart.NorskForelderDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.annenpart.UtenlandskForelderDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.KontoType;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.OppholdsPeriodeDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.OverføringsPeriodeDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.UtsettelsesPeriodeDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.UttaksPeriodeDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ÅpenPeriodeDto;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.AnnenForelderUtenNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Rettigheter;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.UkjentForelder;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Dekningsgrad;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.ObjectFactory;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Dekningsgrader;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.MorsAktivitetsTyper;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Omsorgsovertakelseaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Oppholdsaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Overfoeringsaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Utsettelsesaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Uttaksperiodetyper;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Arbeidsgiver;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Gradering;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.LukketPeriodeMedVedlegg;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Oppholdsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Overfoeringsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Person;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Utsettelsesperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Uttaksperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Virksomhet;
import no.nav.vedtak.felles.xml.soeknad.v3.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

@ApplicationScoped
public class V3ForeldrepengerDomainMapper  {
    private static final FPV3JAXBUtil JAXB = new FPV3JAXBUtil();
    private static final String UKJENT_KODEVERKSVERDI = "-";
    private static final ObjectFactory FP_FACTORY_V3 = new ObjectFactory();
    private static final no.nav.vedtak.felles.xml.soeknad.felles.v3.ObjectFactory FELLES_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.felles.v3.ObjectFactory();
    private static final no.nav.vedtak.felles.xml.soeknad.v3.ObjectFactory SØKNAD_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.v3.ObjectFactory();
    private static final no.nav.vedtak.felles.xml.soeknad.uttak.v3.ObjectFactory UTTAK_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.uttak.v3.ObjectFactory();
    private static final no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.ObjectFactory ENDRING_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.ObjectFactory();

    private Personoppslag personoppslag;

    public V3ForeldrepengerDomainMapper() {
        // CDI
    }

    @Inject
    public V3ForeldrepengerDomainMapper(Personoppslag personoppslag) {
        this.personoppslag = personoppslag;
    }

    public String tilXML(ForeldrepengesøknadDto søknad, LocalDateTime forsendelseMottatt, AktørId søker) {
        return JAXB.marshal(SØKNAD_FACTORY_V3.createSoeknad(tilModell(søknad, forsendelseMottatt, søker)));
    }

    public String tilXML(EndringssøknadForeldrepengerDto endringssøknad, LocalDateTime forsendelseMottatt, AktørId søker) {
        return JAXB.marshal(SØKNAD_FACTORY_V3.createSoeknad(tilModell(endringssøknad, forsendelseMottatt, søker)));
    }

    protected Soeknad tilModell(ForeldrepengesøknadDto søknad, LocalDateTime forsendelseMottatt, AktørId søker) {
        var soeknad = new Soeknad();
        soeknad.setSprakvalg(målformFra(søknad.språkkode()));
        soeknad.getPaakrevdeVedlegg().addAll(vedleggFra(søknad.vedlegg()));
        soeknad.setSoeker(søkerFra(søker, søknad.rolle()));
        soeknad.setOmYtelse(ytelseFraSøknad(søknad));
        soeknad.setMottattDato(forsendelseMottatt.toLocalDate());
        return soeknad;
    }

    private static Soeknad tilModell(EndringssøknadForeldrepengerDto endringsøknad, LocalDateTime forsendelseMottatt, AktørId søker) {
        var soeknad = new Soeknad();
        soeknad.setSprakvalg(målformFra(endringsøknad.språkkode()));
        soeknad.getPaakrevdeVedlegg().addAll(vedleggFra(endringsøknad.vedlegg()));
        soeknad.setSoeker(søkerFra(søker, endringsøknad.rolle()));
        soeknad.setOmYtelse(ytelseFraEndringssøknad(endringsøknad));
        soeknad.setMottattDato(forsendelseMottatt.toLocalDate());
        return soeknad;
    }

    private static JAXBElement<Endringssoeknad> endringssøknadFra(EndringssøknadForeldrepengerDto endring) {
        var endringssoeknad = new Endringssoeknad();
        endringssoeknad.setFordeling(fordelingFra(endring.uttaksplan(), endring.annenForelder(), endring.vedlegg()));
        endringssoeknad.setSaksnummer(endring.saksnummer().value());
        return ENDRING_FACTORY_V3.createEndringssoeknad(endringssoeknad);
    }

    private JAXBElement<Foreldrepenger> foreldrepengerFra(ForeldrepengesøknadDto søknad) {
        return FP_FACTORY_V3.createForeldrepenger(tilForeldrepenger(søknad));
    }

    protected Foreldrepenger tilForeldrepenger(ForeldrepengesøknadDto fp) {
        var foreldrepenger = new Foreldrepenger();
        foreldrepenger.setDekningsgrad(dekningsgradFra(fp.dekningsgrad()));
        foreldrepenger.setMedlemskap(medlemsskapFra(fp.utenlandsopphold(), relasjonDato(fp.barn())));
        foreldrepenger.setOpptjening(opptjeningFra(fp.egenNæring(), fp.frilans(), fp.andreInntekterSiste10Mnd(), fp.vedlegg()));
        foreldrepenger.setFordeling(fordelingFra(fp.uttaksplan(), fp.annenForelder(), fp.vedlegg()));
        foreldrepenger.setRettigheter(rettigheterFra(fp.annenForelder()));
        foreldrepenger.setAnnenForelder(annenForelderFra(fp.annenForelder()));
        foreldrepenger.setRelasjonTilBarnet(relasjonFra(fp.barn(), dokumentasjonSomDokumentererBarn(fp.vedlegg())));
        return foreldrepenger;
    }

    private static OmYtelse ytelseFraEndringssøknad(EndringssøknadForeldrepengerDto endringssøknad) {
        var omYtelse = new OmYtelse();
        omYtelse.getAny().add(endringssøknadFra(endringssøknad));
        return omYtelse;
    }

    private OmYtelse ytelseFraSøknad(ForeldrepengesøknadDto søknad) {
        var omYtelse = new OmYtelse();
        omYtelse.getAny().add(JAXB.marshalToElement(foreldrepengerFra(søknad)));
        return omYtelse;
    }

    private static Dekningsgrad dekningsgradFra(no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.Dekningsgrad dekningsgrad) {
        return Optional.ofNullable(dekningsgrad)
                .map(V3ForeldrepengerDomainMapper::tilDekningsgrad)
                .orElse(null);
    }

    private static Dekningsgrad tilDekningsgrad(no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.Dekningsgrad d) {
        var dekningsgrad = new Dekningsgrad();
        dekningsgrad.setDekningsgrad(tilDekningsgrader(d));
        return dekningsgrad;
    }

    private static Dekningsgrader tilDekningsgrader(no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.Dekningsgrad d) {
        var dekningsgrader = new Dekningsgrader();
        dekningsgrader.setKode(d.verdi());
        return dekningsgrader;
    }

    private static Fordeling fordelingFra(UttaksplanDto uttaksplan, AnnenForelderDto annenForelder, List<VedleggDto> vedlegg) {
        var fordelingXML = new Fordeling();
        fordelingXML.getPerioder().addAll(perioderFra(uttaksplan.uttaksperioder(), vedlegg));
        fordelingXML.setOenskerJustertVedFoedsel(uttaksplan.ønskerJustertUttakVedFødsel());
        fordelingXML.setOenskerKvoteOverfoert(overføringsÅrsakFra(UKJENT_KODEVERKSVERDI));
        fordelingXML.setAnnenForelderErInformert(toBooleanNullSafe(erAnnenpartInformert(annenForelder)));
        return fordelingXML;
    }

    private static Boolean erAnnenpartInformert(AnnenForelderDto annenForelderDto) {
        if (annenForelderDto == null) {
            return false;
        }
        return annenForelderDto.rettigheter().erInformertOmSøknaden();
    }

    private static List<LukketPeriodeMedVedlegg> perioderFra(List<Uttaksplanperiode> perioder, List<VedleggDto> vedlegg) {
        return safeStream(perioder)
                .map(periode -> lukketPeriodeFra(periode, vedlegg))
                .toList();
    }

    private static List<JAXBElement<Object>> lukketPeriodeVedleggFra(List<UUID> vedlegg) {
        return safeStream(vedlegg)
                .filter(Objects::nonNull)
                .map(referanse -> UTTAK_FACTORY_V3.createLukketPeriodeMedVedleggVedlegg(tilVedlegg(referanse)))
                .toList();
    }

    private static LukketPeriodeMedVedlegg lukketPeriodeFra(Uttaksplanperiode periode, List<VedleggDto> vedlegg) {
        var vedleggreferanser = dokumentasjonSomDokumentererUttaksperiode(vedlegg, new ÅpenPeriodeDto(periode.fom(), periode.tom()));
        return switch (periode) {
            case UttaksPeriodeDto uttak -> uttak.gradering() != null ? createGradertUttaksperiode(uttak, vedleggreferanser) : createUttaksperiode(uttak, vedleggreferanser);
            case OppholdsPeriodeDto opphold -> createOppholsperiode(opphold, vedleggreferanser);
            case UtsettelsesPeriodeDto utsettelse -> UtsettelsesÅrsak.FRI.equals(utsettelse.årsak()) ? createFriUtsettelsesPeriode(utsettelse, vedleggreferanser) : createUtsettelsesperiode(utsettelse, vedleggreferanser);
            case OverføringsPeriodeDto overføring -> createOverføringsperiode(overføring, vedleggreferanser);
        };
    }

    private static LukketPeriodeMedVedlegg createOverføringsperiode(OverføringsPeriodeDto o, List<UUID> vedleggreferanser) {
        var overfoeringsperiode = new Overfoeringsperiode();
        overfoeringsperiode.setFom(o.fom());
        overfoeringsperiode.setTom(o.tom());
        overfoeringsperiode.setOverfoeringAv(uttaksperiodeTypeFra(o.konto()));
        overfoeringsperiode.setAarsak(påkrevdOverføringsÅrsakFra(o.årsak()));
        overfoeringsperiode.getVedlegg().addAll(lukketPeriodeVedleggFra(vedleggreferanser));
        return overfoeringsperiode;
    }

    private static LukketPeriodeMedVedlegg createOppholsperiode(OppholdsPeriodeDto o, List<UUID> vedleggreferanser) {
        var oppholdsperiode = new Oppholdsperiode();
        oppholdsperiode.setFom(o.fom());
        oppholdsperiode.setTom(o.tom());
        oppholdsperiode.setAarsak(oppholdsÅrsakFra(o.årsak()));
        oppholdsperiode.getVedlegg().addAll(lukketPeriodeVedleggFra(vedleggreferanser));
        return oppholdsperiode;
    }

    private static LukketPeriodeMedVedlegg createUtsettelsesperiode(UtsettelsesPeriodeDto u, List<UUID> vedleggreferanser) {
        var utsettelsesperiode = new Utsettelsesperiode();
        utsettelsesperiode.setFom(u.fom());
        utsettelsesperiode.setTom(u.tom());
        utsettelsesperiode.setErArbeidstaker(u.erArbeidstaker());
        utsettelsesperiode.setMorsAktivitetIPerioden(morsAktivitetFra(u.morsAktivitetIPerioden()));
        utsettelsesperiode.setAarsak(utsettelsesÅrsakFra(u.årsak()));
        utsettelsesperiode.getVedlegg().addAll(lukketPeriodeVedleggFra(vedleggreferanser));
        return utsettelsesperiode;
    }

    private static LukketPeriodeMedVedlegg createFriUtsettelsesPeriode(UtsettelsesPeriodeDto p, List<UUID> vedleggreferanser) {
        var utsettelsesperiode = new Utsettelsesperiode();
        utsettelsesperiode.setFom(p.fom());
        utsettelsesperiode.setTom(p.tom());
        utsettelsesperiode.setMorsAktivitetIPerioden(morsAktivitetFra(p.morsAktivitetIPerioden(), true));
        utsettelsesperiode.setAarsak(utsettelsesÅrsakFra(p.årsak()));
        utsettelsesperiode.getVedlegg().addAll(lukketPeriodeVedleggFra(vedleggreferanser));
        return utsettelsesperiode;
    }

    private static LukketPeriodeMedVedlegg createGradertUttaksperiode(UttaksPeriodeDto g, List<UUID> vedleggreferanser) {
        var graderingDTO = g.gradering();
        var gradering = new Gradering();
        gradering.setFom(g.fom());
        gradering.setTom(g.tom());
        gradering.setType(uttaksperiodeTypeFra(g.konto()));
        gradering.setOenskerSamtidigUttak(g.ønskerSamtidigUttak());
        gradering.setMorsAktivitetIPerioden(morsAktivitetFra(g.morsAktivitetIPerioden()));
        gradering.setOenskerFlerbarnsdager(g.ønskerFlerbarnsdager());
        gradering.setArbeidtidProsent(graderingDTO.stillingsprosent());
        gradering.setArbeidsgiver(arbeidsgiverFra(graderingDTO.orgnumre()));
        gradering.setArbeidsforholdSomSkalGraderes(true);
        Optional.ofNullable(graderingDTO.erArbeidstaker()).ifPresent(gradering::setErArbeidstaker);
        Optional.ofNullable(graderingDTO.erFrilanser()).ifPresent(gradering::setErFrilanser);
        Optional.ofNullable(graderingDTO.erSelvstendig()).ifPresent(gradering::setErSelvstNæringsdrivende);
        gradering.getVedlegg().addAll(lukketPeriodeVedleggFra(vedleggreferanser));

        if (Boolean.TRUE.equals(g.ønskerSamtidigUttak())) {
            gradering.setSamtidigUttakProsent(g.samtidigUttakProsent());
        }

        return gradering;
    }

    private static LukketPeriodeMedVedlegg createUttaksperiode(UttaksPeriodeDto u, List<UUID> vedleggreferanser) {
        var uttaksperiode = new Uttaksperiode();
        uttaksperiode.setFom(u.fom());
        uttaksperiode.setTom(u.tom());
        uttaksperiode.setSamtidigUttakProsent(u.samtidigUttakProsent());
        uttaksperiode.setOenskerFlerbarnsdager(u.ønskerFlerbarnsdager());
        uttaksperiode.setType(uttaksperiodeTypeFra(u.konto()));
        uttaksperiode.setOenskerSamtidigUttak(u.ønskerSamtidigUttak());
        uttaksperiode.setMorsAktivitetIPerioden(morsAktivitetFra(u.morsAktivitetIPerioden()));
        uttaksperiode.getVedlegg().addAll(lukketPeriodeVedleggFra(vedleggreferanser));
        return uttaksperiode;
    }

    private static Arbeidsgiver arbeidsgiverFra(List<String> arbeidsgiver) {
        if (arbeidsgiver == null || arbeidsgiver.isEmpty()) {
            return null;
        }
        return Optional.ofNullable(arbeidsgiver.get(0))
                .map(V3ForeldrepengerDomainMapper::arbeidsgiverFra)
                .orElse(null);
    }

    private static Arbeidsgiver arbeidsgiverFra(String id) {
        return switch (id.length()) {
            case 11 -> tilPerson(id);
            case 9 -> tilArbeidsgiver(id);
            default -> throw new UnexpectedInputException("Ugyldig lengde " + id.length() + " for arbeidsgiver");
        };
    }

    private static Arbeidsgiver tilArbeidsgiver(String orgnummer) {
        var virksomhet = new Virksomhet();
        virksomhet.setIdentifikator(orgnummer);
        return virksomhet;
    }

    private static Arbeidsgiver tilPerson(String fnr) {
        var person = new Person();
        person.setIdentifikator(fnr);
        return person;
    }

    private static Uttaksperiodetyper uttaksperiodeTypeFra(KontoType type) {
        var periodeType = new Uttaksperiodetyper();
        periodeType.setKode(type.getKode());
        periodeType.setKodeverk(periodeType.getKodeverk());
        return periodeType;
    }

    private static MorsAktivitetsTyper morsAktivitetFra(MorsAktivitet aktivitet) {
        return morsAktivitetFra(aktivitet, false);
    }

    private static MorsAktivitetsTyper morsAktivitetFra(MorsAktivitet aktivitet, boolean optional) {
        if (optional) {
            return Optional.ofNullable(aktivitet)
                    .map(MorsAktivitet::name)
                    .map(V3ForeldrepengerDomainMapper::morsAktivitetFra)
                    .orElse(null);
        }
        return Optional.ofNullable(aktivitet)
                .map(MorsAktivitet::name)
                .map(V3ForeldrepengerDomainMapper::morsAktivitetFra)
                .orElse(morsAktivitetFra(UKJENT_KODEVERKSVERDI));
    }

    private static MorsAktivitetsTyper morsAktivitetFra(String aktivitet) {
        var morsAktivitet = new MorsAktivitetsTyper();
        morsAktivitet.setKode(aktivitet);
        morsAktivitet.setKodeverk(morsAktivitet.getKodeverk()); // TODO
        return morsAktivitet;
    }

    private static Utsettelsesaarsaker utsettelsesÅrsakFra(UtsettelsesÅrsak årsak) {
        return Optional.ofNullable(årsak)
                .map(UtsettelsesÅrsak::name)
                .map(V3ForeldrepengerDomainMapper::utsettelsesÅrsakFra)
                .orElse(null);
    }

    private static Utsettelsesaarsaker utsettelsesÅrsakFra(String årsak) {
        var utsettelsesÅrsak = new Utsettelsesaarsaker();
        utsettelsesÅrsak.setKode(årsak);
        utsettelsesÅrsak.setKodeverk(utsettelsesÅrsak.getKodeverk());
        return utsettelsesÅrsak;
    }

    private static Oppholdsaarsaker oppholdsÅrsakFra(Oppholdsårsak årsak) {
        return Optional.ofNullable(årsak)
                .map(Oppholdsårsak::name)
                .map(V3ForeldrepengerDomainMapper::oppholdsÅrsakFra)
                .orElseThrow(() -> new UnexpectedInputException("Oppholdsårsak må være satt"));
    }

    private static Oppholdsaarsaker oppholdsÅrsakFra(String årsak) {
        var oppholdsÅrsak = new Oppholdsaarsaker();
        oppholdsÅrsak.setKode(årsak);
        oppholdsÅrsak.setKodeverk(oppholdsÅrsak.getKodeverk());
        return oppholdsÅrsak;
    }

    private static Overfoeringsaarsaker påkrevdOverføringsÅrsakFra(Overføringsårsak årsak) {
        return Optional.ofNullable(årsak)
                .map(Overføringsårsak::name)
                .map(V3ForeldrepengerDomainMapper::overføringsÅrsakFra)
                .orElseThrow(() -> new UnexpectedInputException("Oppholdsårsak må være satt"));
    }

    private static Overfoeringsaarsaker overføringsÅrsakFra(String årsak) {
        var overføringsÅrsak = new Overfoeringsaarsaker();
        overføringsÅrsak.setKode(årsak);
        overføringsÅrsak.setKodeverk(overføringsÅrsak.getKodeverk());
        return overføringsÅrsak;
    }

    private static Rettigheter rettigheterFra(AnnenForelderDto annenforelder) {
        return Optional.ofNullable(annenforelder)
            .map(AnnenForelderDto::rettigheter)
            .map(V3ForeldrepengerDomainMapper::create)
            .orElse(rettigheterForUkjentForelder());
    }

    private static Rettigheter rettigheterForUkjentForelder() {
        var rettigheter = new Rettigheter();
        rettigheter.setHarOmsorgForBarnetIPeriodene(true);
        rettigheter.setHarAnnenForelderRett(false);
        rettigheter.setHarAleneomsorgForBarnet(true);
        return rettigheter;
    }

    private static Rettigheter create(AnnenForelderDto.Rettigheter rettigheterDto) {
        var rettigheter = new Rettigheter();
        rettigheter.setHarOmsorgForBarnetIPeriodene(true);
        rettigheter.setHarAnnenForelderRett(rettigheterDto.harRettPåForeldrepenger());
        rettigheter.setHarAleneomsorgForBarnet(toBooleanNullSafe(rettigheterDto.erAleneOmOmsorg())); // TODO: Må være satt, selv om Boolean undefined..
        rettigheter.setHarMorUforetrygd(toBooleanNullSafe(rettigheterDto.harMorUføretrygd())); // TODO: Må være satt, selv om Boolean undefined..
        rettigheter.setHarAnnenForelderOppholdtSegIEOS(rettigheterDto.harAnnenForelderOppholdtSegIEØS());
        rettigheter.setHarAnnenForelderTilsvarendeRettEOS(toBooleanNullSafe(rettigheterDto.harAnnenForelderTilsvarendeRettEØS())); // TODO: Må være satt, selv om Boolean undefined..
        return rettigheter;
    }

    private AnnenForelder annenForelderFra(AnnenForelderDto annenForelder) {
        return switch (annenForelder) {
            case NorskForelderDto norsk -> norskForelder(norsk);
            case UtenlandskForelderDto utenlandsk -> utenlandskForelder(utenlandsk);
            case null, default -> new UkjentForelder();
        };
    }

    private static AnnenForelderUtenNorskIdent utenlandskForelder(UtenlandskForelderDto utenlandskForelder) {
        var annenForelderUtenNorskIdent = new AnnenForelderUtenNorskIdent();
        annenForelderUtenNorskIdent.setUtenlandskPersonidentifikator(utenlandskForelder.fnr().value());
        annenForelderUtenNorskIdent.setLand(landFra(utenlandskForelder.bostedsland()));
        return annenForelderUtenNorskIdent;
    }

    private AnnenForelderMedNorskIdent norskForelder(NorskForelderDto norskForelder) {
        var annenForelderMedNorskIdent = new AnnenForelderMedNorskIdent();
        annenForelderMedNorskIdent.setAktoerId(personoppslag.aktørId(norskForelder.fnr()).value());
        return annenForelderMedNorskIdent;
    }

    private static SoekersRelasjonTilBarnet relasjonFra(BarnDto barnDto, List<UUID> vedleggReferanser) {
        return switch (barnDto) {
            case AdopsjonDto adopsjonDto -> createAdopsjon(adopsjonDto, vedleggReferanser);
            case FødselDto fødselDto -> createFødsel(fødselDto, vedleggReferanser);
            case OmsorgsovertakelseDto omsorgsovertakelseDto -> createOmsorgsovertakelse(omsorgsovertakelseDto, vedleggReferanser);
            case TerminDto terminDto -> createTermin(terminDto, vedleggReferanser);
        };
    }

    private static SoekersRelasjonTilBarnet createOmsorgsovertakelse(OmsorgsovertakelseDto omsorgsovertakelse, List<UUID> vedleggReferanser) {
        var omsorgsovertakelseXLM = new Omsorgsovertakelse();
        omsorgsovertakelseXLM.getVedlegg().addAll(relasjonTilBarnVedleggFra(vedleggReferanser));
        omsorgsovertakelseXLM.setAntallBarn(omsorgsovertakelse.antallBarn());
        omsorgsovertakelseXLM.getFoedselsdato().addAll(omsorgsovertakelse.fødselsdatoer());
        omsorgsovertakelseXLM.setOmsorgsovertakelsesdato(omsorgsovertakelse.foreldreansvarsdato());
        var omsorgsovertakelseaarsaker = new Omsorgsovertakelseaarsaker();
        omsorgsovertakelseaarsaker.setKode("OVERTATT_OMSORG");
        omsorgsovertakelseXLM.setOmsorgsovertakelseaarsak(omsorgsovertakelseaarsaker);
        omsorgsovertakelseXLM.setBeskrivelse("Omsorgsovertakelse");
        return omsorgsovertakelseXLM;
    }

    private static SoekersRelasjonTilBarnet createFødsel(FødselDto fødsel, List<UUID> vedleggReferanser) {
        var foedsel = new Foedsel();
        foedsel.getVedlegg().addAll(relasjonTilBarnVedleggFra(vedleggReferanser));
        foedsel.setFoedselsdato(fødsel.fødselsdato());
        foedsel.setTermindato(fødsel.termindato());
        foedsel.setAntallBarn(fødsel.antallBarn());
        return foedsel;
    }

    private static SoekersRelasjonTilBarnet createTermin(TerminDto termin, List<UUID> vedleggReferanser) {
        var terminXML = new Termin();
        terminXML.getVedlegg().addAll(relasjonTilBarnVedleggFra(vedleggReferanser));
        terminXML.setTermindato(termin.termindato());
        terminXML.setUtstedtdato(termin.terminbekreftelseDato());
        terminXML.setAntallBarn(termin.antallBarn());
        return terminXML;
    }

    private static SoekersRelasjonTilBarnet createAdopsjon(AdopsjonDto adopsjon, List<UUID> vedleggReferanser) {
        var adopsjonXML = new Adopsjon();
        adopsjonXML.getVedlegg().addAll(relasjonTilBarnVedleggFra(vedleggReferanser));
        adopsjonXML.setAntallBarn(adopsjon.antallBarn());
        adopsjonXML.getFoedselsdato().addAll(adopsjon.fødselsdatoer());
        adopsjonXML.setOmsorgsovertakelsesdato(adopsjon.adopsjonsdato());
        adopsjonXML.setAdopsjonAvEktefellesBarn(adopsjon.adopsjonAvEktefellesBarn());
        adopsjonXML.setAnkomstdato(adopsjon.ankomstdato());
        return adopsjonXML;
    }

    private static List<JAXBElement<Object>> relasjonTilBarnVedleggFra(List<UUID> vedlegg) {
        return safeStream(vedlegg)
                .filter(Objects::nonNull)
                .map(referanse -> FELLES_FACTORY_V3.createSoekersRelasjonTilBarnetVedlegg(tilVedlegg(referanse)))
                .toList();
    }
}
