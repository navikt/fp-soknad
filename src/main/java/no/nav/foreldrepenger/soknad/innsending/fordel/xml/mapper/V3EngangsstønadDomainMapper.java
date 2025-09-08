package no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper;

import static no.nav.foreldrepenger.common.util.StreamUtil.safeStream;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.DokumentasjonReferanseMapper.dokumentasjonSomDokumentererBarn;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.medlemsskapFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.målformFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.relasjonDato;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.søkerFra;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.tilVedlegg;
import static no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3DomainMapperCommon.vedleggFra;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.felles.Vedlegg;
import no.nav.foreldrepenger.common.domain.felles.relasjontilbarn.FremtidigFødsel;
import no.nav.foreldrepenger.common.domain.felles.relasjontilbarn.Fødsel;
import no.nav.foreldrepenger.common.error.UnexpectedInputException;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.jaxb.ESV3JAXBUtil;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AdopsjonDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.BarnDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.FødselDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.OmsorgsovertakelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.TerminDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggReferanse;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v3.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v3.ObjectFactory;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Termin;
import no.nav.vedtak.felles.xml.soeknad.v3.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

@ApplicationScoped
public class V3EngangsstønadDomainMapper {
    private static final ESV3JAXBUtil JAXB = new ESV3JAXBUtil();
    private static final ObjectFactory ES_FACTORY_V3 = new ObjectFactory();
    private static final no.nav.vedtak.felles.xml.soeknad.v3.ObjectFactory SØKNAD_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.v3.ObjectFactory();
    private static final no.nav.vedtak.felles.xml.soeknad.felles.v3.ObjectFactory FELLES_FACTORY_V3 = new no.nav.vedtak.felles.xml.soeknad.felles.v3.ObjectFactory();


    public String tilXML(EngangsstønadDto søknad, AktørId søker) {
        return JAXB.marshal(SØKNAD_FACTORY_V3.createSoeknad(tilModell(søknad, søker)));
    }

    private Soeknad tilModell(EngangsstønadDto søknad, AktørId søker) {
        var soeknad = new Soeknad();
        soeknad.setSprakvalg(målformFra(søknad.språkkode()));
        soeknad.getPaakrevdeVedlegg().addAll(vedleggFra(søknad.vedlegg()));
        soeknad.setSoeker(søkerFra(søker, søknad.rolle()));
        soeknad.setMottattDato(søknad.mottattdato());
        soeknad.setOmYtelse(ytelseFra(søknad));
        return soeknad;
    }

    private OmYtelse ytelseFra(EngangsstønadDto søknad) {
        var omYtelse = new OmYtelse();
        omYtelse.getAny().add(JAXB.marshalToElement(engangsstønadFra(søknad)));
        return omYtelse;
    }

    private JAXBElement<Engangsstønad> engangsstønadFra(EngangsstønadDto es) {
        var engangsstønad = new Engangsstønad();
        engangsstønad.setMedlemskap(medlemsskapFra(es.utenlandsopphold(), relasjonDato(es.barn())));
        engangsstønad.setSoekersRelasjonTilBarnet(relasjonFra(es.barn(), es.vedlegg()));
        return ES_FACTORY_V3.createEngangsstønad(engangsstønad);
    }

    private static SoekersRelasjonTilBarnet relasjonFra(BarnDto barn, List<VedleggDto> vedlegg) {
        return switch (barn) {
            case AdopsjonDto a -> create(a, dokumentasjonSomDokumentererBarn(vedlegg));
            case FødselDto f -> create(f, dokumentasjonSomDokumentererBarn(vedlegg));
            case TerminDto t -> create(t, dokumentasjonSomDokumentererBarn(vedlegg));
            case OmsorgsovertakelseDto ignored -> throw new UnexpectedInputException("Omsorgsovertakelse er ikke støttet for engangsstønad");
        };
    }

    private static SoekersRelasjonTilBarnet create(AdopsjonDto adopsjon, List<VedleggReferanse> vedlegg) {
        var adopsjonXML = new Adopsjon();
        adopsjonXML.getVedlegg().addAll(relasjonTilBarnVedleggFra(vedlegg));
        adopsjonXML.setAntallBarn(adopsjon.antallBarn());
        adopsjonXML.getFoedselsdato().addAll(adopsjon.fødselsdatoer());
        adopsjonXML.setOmsorgsovertakelsesdato(adopsjon.adopsjonsdato());
        adopsjonXML.setAdopsjonAvEktefellesBarn(adopsjon.adopsjonAvEktefellesBarn());
        adopsjonXML.setAnkomstdato(adopsjon.ankomstdato());
        return adopsjonXML;
    }

    private static SoekersRelasjonTilBarnet create(FødselDto fødsel, List<VedleggReferanse> vedlegg) {
        var foedsel = new Foedsel();
        foedsel.getVedlegg().addAll(relasjonTilBarnVedleggFra(vedlegg));
        foedsel.setFoedselsdato(fødsel.fødselsdato());
        foedsel.setTermindato(fødsel.termindato());
        foedsel.setAntallBarn(fødsel.antallBarn());
        return foedsel;
    }

    private static SoekersRelasjonTilBarnet create(TerminDto termin, List<VedleggReferanse> vedlegg) {
        var terminXML = new Termin();
        terminXML.getVedlegg().addAll(relasjonTilBarnVedleggFra(vedlegg));
        terminXML.setTermindato(termin.termindato());
        terminXML.setUtstedtdato(termin.terminbekreftelseDato());
        terminXML.setAntallBarn(termin.antallBarn());
        return terminXML;
    }

    private static List<JAXBElement<Object>> relasjonTilBarnVedleggFra(List<VedleggReferanse> vedlegg) {
        return safeStream(vedlegg)
            .map(VedleggReferanse::verdi)
            .map(referanse -> FELLES_FACTORY_V3.createSoekersRelasjonTilBarnetVedlegg(tilVedlegg(referanse)))
            .toList();
    }
}
