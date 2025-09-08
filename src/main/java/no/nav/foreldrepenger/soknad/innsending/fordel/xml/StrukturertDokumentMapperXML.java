package no.nav.foreldrepenger.soknad.innsending.fordel.xml;

import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.PersonOppslagTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V1SvangerskapspengerDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3EngangsstønadDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3ForeldrepengerDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;

@ApplicationScoped
public class StrukturertDokumentMapperXML {

    private V3ForeldrepengerDomainMapper fpMapper;
    private V1SvangerskapspengerDomainMapper svpMapper;
    private V3EngangsstønadDomainMapper esMapper;
    private PersonOppslagTjeneste personOppslagTjeneste;
    private DokumentRepository dokumentRepository;

    public StrukturertDokumentMapperXML() {
        // CDI
    }

    @Inject
    public StrukturertDokumentMapperXML(V3ForeldrepengerDomainMapper fpMapper, V1SvangerskapspengerDomainMapper svpMapper, V3EngangsstønadDomainMapper esMapper,
                                        PersonOppslagTjeneste personOppslagTjeneste, DokumentRepository dokumentRepository) {
        this.fpMapper = fpMapper;
        this.svpMapper = svpMapper;
        this.esMapper = esMapper;
        this.personOppslagTjeneste = personOppslagTjeneste;
        this.dokumentRepository = dokumentRepository;
    }


    public Dokument lagStrukturertDokumentForArkivering(DokumentMetadata metadata, Dokument søknad) {
        var søknadJson = SøknadJsonMapper.deseraliserSøknad(søknad);
        var xml = mapSøknadTilXML(søknadJson, personOppslagTjeneste.hentAkøridFor(metadata.getBrukersFnr()));
        var xmlDokument = Dokument.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setErSøknad(true)
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(xml.getBytes(StandardCharsets.UTF_8), ArkivFilType.XML)
            .build();
        dokumentRepository.lagre(xmlDokument); // TODO: Lagre ned dokument? Eller sende i payload?
        return xmlDokument;
    }


    private String mapSøknadTilXML(SøknadDto søknad, AktørId søker) {
        return switch (søknad) {
            case ForeldrepengesøknadDto fp -> fpMapper.tilXML(fp, søker);
            case EndringssøknadForeldrepengerDto endringFp -> fpMapper.tilXML(endringFp, søker);
            case EngangsstønadDto es -> esMapper.tilXML(es, søker);
            case SvangerskapspengesøknadDto svp -> svpMapper.tilXML(svp, søker);
        };
    }

}
