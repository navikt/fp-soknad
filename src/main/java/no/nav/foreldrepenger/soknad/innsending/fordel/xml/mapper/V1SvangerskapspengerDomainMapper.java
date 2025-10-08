package no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper;

import static no.nav.foreldrepenger.common.util.StreamUtil.safeStream;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.DokumentasjonReferanseMapper.dokumentasjonSomDokumentererTilrettelegggingAv;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.medlemsskapFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.målformFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.opptjeningFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.søkerFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.tilVedlegg;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.vedleggFra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.error.UnexpectedInputException;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.jaxb.SVPV1JAXBUtil;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.ArbeidsforholdDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.AvtaltFerieDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.TilretteleggingbehovDto;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Arbeidsforhold;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Arbeidsgiver;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.AvtaltFerie;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.AvtaltFerieListe;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.DelvisTilrettelegging;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Frilanser;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.HelTilrettelegging;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.IngenTilrettelegging;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.ObjectFactory;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.PrivatArbeidsgiver;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.SelvstendigNæringsdrivende;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Svangerskapspenger;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Tilrettelegging;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.TilretteleggingListe;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Virksomhet;
import no.nav.vedtak.felles.xml.soeknad.v3.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

@ApplicationScoped
public class V1SvangerskapspengerDomainMapper {
    private static final SVPV1JAXBUtil jaxb = new SVPV1JAXBUtil();
    private static final ObjectFactory SVP_FACTORY_V1 = new ObjectFactory();
    private static final no.nav.vedtak.felles.xml.soeknad.v3.ObjectFactory SØKNAD_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.v3.ObjectFactory();

    public String tilXML(SvangerskapspengesøknadDto svp, LocalDateTime forsendelseMottatt, AktørId søker) {
        return jaxb.marshal(SØKNAD_FACTORY_V3.createSoeknad(tilModell(svp, forsendelseMottatt, søker)));
    }

    public Soeknad tilModell(SvangerskapspengesøknadDto svp, LocalDateTime forsendelseMottatt, AktørId søker) {
        var soeknad = new Soeknad();
        soeknad.setSprakvalg(målformFra(svp.språkkode()));
        soeknad.getPaakrevdeVedlegg().addAll(vedleggFra(svp.vedlegg()));
        soeknad.setSoeker(søkerFra(søker, svp.rolle()));
        soeknad.setOmYtelse(ytelseFra(svp));
        soeknad.setMottattDato(forsendelseMottatt.toLocalDate());
        return soeknad;
    }

    private OmYtelse ytelseFra(SvangerskapspengesøknadDto svp) {
        var omYtelse = new OmYtelse();
        omYtelse.getAny().add(jaxb.marshalToElement(svangerskapspengerFra(svp)));
        return omYtelse;
    }

    private static JAXBElement<Svangerskapspenger> svangerskapspengerFra(SvangerskapspengesøknadDto svp) {
        var svangerskapspenger = tilSvangerskapspenger(svp);
        return SVP_FACTORY_V1.createSvangerskapspenger(svangerskapspenger);
    }

    protected static Svangerskapspenger tilSvangerskapspenger(SvangerskapspengesøknadDto svp) {
        var svangerskapspenger = new Svangerskapspenger();
        svangerskapspenger.setTermindato(svp.barnSvp().termindato());
        svangerskapspenger.setFødselsdato(svp.barnSvp().fødselsdato());
        svangerskapspenger.setOpptjening(opptjeningFra(svp.egenNæring(), svp.frilans(), svp.andreInntekterSiste10Mnd(), svp.vedlegg()));
        svangerskapspenger.setAvtaltFerieListe(opprettAvtaltFerieListe(svp.avtaltFerie()));
        svangerskapspenger.setTilretteleggingListe(tilretteleggingListeFra(svp.tilretteleggingsbehov(), svp.vedlegg()));
        svangerskapspenger.setMedlemskap(medlemsskapFra(svp.utenlandsopphold(), relasjonsDatoFra(svp.barnSvp().termindato(), svp.barnSvp().fødselsdato())));
        return svangerskapspenger;
    }

    private static AvtaltFerieListe opprettAvtaltFerieListe(List<AvtaltFerieDto> avtaltFerier) {
        var mappetFerieListe = safeStream(avtaltFerier)
                .map(af -> {
                    var avtaltFerie = new AvtaltFerie();
                    Arbeidsgiver arbeidsgiver = mapArbeidsgiver(af);
                    avtaltFerie.setArbeidsgiver(arbeidsgiver);
                    avtaltFerie.setAvtaltFerieFom(af.fom());
                    avtaltFerie.setAvtaltFerieTom(af.tom());
                    return avtaltFerie;
                }).toList();
        var avtaltFerieListe = new AvtaltFerieListe();
        avtaltFerieListe.getAvtaltFerie().addAll(mappetFerieListe);
        return avtaltFerieListe;
    }

    private static Arbeidsgiver mapArbeidsgiver(AvtaltFerieDto af) {
        return switch (af.arbeidsforhold()) {
            case ArbeidsforholdDto.VirksomhetDto(var orgnr) -> {
                var virksomhetXml = new Virksomhet();
                virksomhetXml.setIdentifikator(orgnr.value());
                yield virksomhetXml;
            }
            case ArbeidsforholdDto.PrivatArbeidsgiverDto(var fnr) -> {
                var privatArbeidsgiverXml = new PrivatArbeidsgiver();
                privatArbeidsgiverXml.setIdentifikator(fnr.value());
                yield privatArbeidsgiverXml;
            }
            case ArbeidsforholdDto.SelvstendigNæringsdrivendeDto ignored ->
                    throw new IllegalStateException("Oppgitt ferie er ikke støttet for selvstendig næringsdrivende eller frilansere");
            case ArbeidsforholdDto.FrilanserDto ignored ->
                    throw new IllegalStateException("Oppgitt ferie er ikke støttet for selvstendig næringsdrivende eller frilansere");
            default -> throw new IllegalStateException("Unexpected value: " + af.arbeidsforhold()); // Permits?
        };
    }

    private static TilretteleggingListe tilretteleggingListeFra(List<TilretteleggingbehovDto> tilretteleggingbehov, List<VedleggDto> vedlegg) {
        var tilretteleggingListe = new TilretteleggingListe();
        tilretteleggingListe.getTilrettelegging().addAll(
                safeStream(tilretteleggingbehov)
                .map(t -> tilretteleggingFra(t, vedlegg))
                .toList());
        return tilretteleggingListe;
    }

    private static Tilrettelegging tilretteleggingFra(TilretteleggingbehovDto tilretteleggingbehov, List<VedleggDto> vedlegg) {
        var tilrettelegging = new Tilrettelegging();
        tilrettelegging.setBehovForTilretteleggingFom(tilretteleggingbehov.behovForTilretteleggingFom());
        tilrettelegging.setArbeidsforhold(arbeidsforholdFra(tilretteleggingbehov));
        tilrettelegging.getVedlegg().addAll(tilretteleggingVedleggFraIDs(dokumentasjonSomDokumentererTilrettelegggingAv(vedlegg, tilretteleggingbehov.arbeidsforhold())));

        for (var t : tilretteleggingbehov.tilrettelegginger()) {
            switch (t) {
                case TilretteleggingbehovDto.TilretteleggingDto.Hel hel -> tilrettelegging.getHelTilrettelegging().add(tilHelTilrettelegging(hel));
                case TilretteleggingbehovDto.TilretteleggingDto.Del del -> tilrettelegging.getDelvisTilrettelegging().add(tilDelTilrettelegging(del));
                case TilretteleggingbehovDto.TilretteleggingDto.Ingen ingen -> tilrettelegging.getIngenTilrettelegging().add(tilIngenTilrettelegging(ingen));
                default -> throw new UnexpectedInputException("Ukjent tilrettelegging %s", tilrettelegging.getClass().getSimpleName());
            }
        }
        return tilrettelegging;
    }

    private static IngenTilrettelegging tilIngenTilrettelegging(TilretteleggingbehovDto.TilretteleggingDto.Ingen ingen) {
        var ingenTilrettelegging = new IngenTilrettelegging();
        ingenTilrettelegging.setSlutteArbeidFom(ingen.fom());
        return ingenTilrettelegging;
    }

    private static DelvisTilrettelegging tilDelTilrettelegging(TilretteleggingbehovDto.TilretteleggingDto.Del del) {
        var delvisTilrettelegging = new DelvisTilrettelegging();
        delvisTilrettelegging.setTilrettelagtArbeidFom(del.fom());
        delvisTilrettelegging.setStillingsprosent(BigDecimal.valueOf(del.stillingsprosent()));
        return delvisTilrettelegging;
    }

    private static HelTilrettelegging tilHelTilrettelegging(TilretteleggingbehovDto.TilretteleggingDto.Hel hel) {
        var helTilrettelegging = new HelTilrettelegging();
        helTilrettelegging.setTilrettelagtArbeidFom(hel.fom());
        return helTilrettelegging;
    }

    private static List<JAXBElement<Object>> tilretteleggingVedleggFraIDs(List<UUID> vedlegg) {
        return safeStream(vedlegg)
                .filter(Objects::nonNull)
                .map(referanse -> SVP_FACTORY_V1.createTilretteleggingVedlegg(tilVedlegg(referanse)))
                .toList();
    }

    private static Arbeidsforhold arbeidsforholdFra(TilretteleggingbehovDto tilretteleggingbehov) {
        var arbeidsforhold = tilretteleggingbehov.arbeidsforhold();
        if (arbeidsforhold instanceof ArbeidsforholdDto.VirksomhetDto(var orgnr)) {
            var virksomhet = new Virksomhet();
            virksomhet.setIdentifikator(orgnr.value());
            return virksomhet;
        }
        if (arbeidsforhold instanceof ArbeidsforholdDto.PrivatArbeidsgiverDto(var fnr)) {
            var privatArbeidsgiver = new PrivatArbeidsgiver();
            privatArbeidsgiver.setIdentifikator(fnr.value());
            return privatArbeidsgiver;
        }

        if (arbeidsforhold instanceof ArbeidsforholdDto.FrilanserDto) {
            var frilanser = new Frilanser();
            frilanser.setOpplysningerOmTilretteleggingstiltak(tilretteleggingbehov.tilretteleggingstiltak());
            frilanser.setOpplysningerOmRisikofaktorer(tilretteleggingbehov.risikofaktorer());
            return frilanser;
        }

        if (arbeidsforhold instanceof ArbeidsforholdDto.SelvstendigNæringsdrivendeDto) {
            var selvstendigNæringsdrivende = new SelvstendigNæringsdrivende();
            selvstendigNæringsdrivende.setOpplysningerOmTilretteleggingstiltak(tilretteleggingbehov.tilretteleggingstiltak());
            selvstendigNæringsdrivende.setOpplysningerOmRisikofaktorer(tilretteleggingbehov.risikofaktorer());
            return selvstendigNæringsdrivende;
        }

        throw new UnexpectedInputException("Ukjent arbeidsforhold %s", arbeidsforhold.getClass().getSimpleName());
    }

    private static LocalDate relasjonsDatoFra(LocalDate termindato, LocalDate fødselsdato) {
        return Optional.ofNullable(fødselsdato)
                .orElse(termindato);
    }
}
