package no.nav.foreldrepenger.soknad.innsending.fordel.xml;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fpsoknad.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.EngangsstønadDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.SøknadDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.AktørId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.Personoppslag;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V1SvangerskapspengerDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3EngangsstønadDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3ForeldrepengerDomainMapper;

@ApplicationScoped
public class StrukturertDokumentMapperXML {

    private V3ForeldrepengerDomainMapper fpMapper;
    private V1SvangerskapspengerDomainMapper svpMapper;
    private V3EngangsstønadDomainMapper esMapper;
    private Personoppslag personoppslag;
    private DokumentRepository dokumentRepository;

    public StrukturertDokumentMapperXML() {
        // CDI
    }

    @Inject
    public StrukturertDokumentMapperXML(V3ForeldrepengerDomainMapper fpMapper, V1SvangerskapspengerDomainMapper svpMapper, V3EngangsstønadDomainMapper esMapper,
                                        Personoppslag personoppslag, DokumentRepository dokumentRepository) {
        this.fpMapper = fpMapper;
        this.svpMapper = svpMapper;
        this.esMapper = esMapper;
        this.personoppslag = personoppslag;
        this.dokumentRepository = dokumentRepository;
    }


    public DokumentEntitet lagStrukturertDokumentForArkivering(ForsendelseEntitet metadata, DokumentEntitet søknad) {
        var søknadJson = SøknadJsonMapper.deseraliserSøknad(søknad);
        var xml = mapSøknadTilXML(søknadJson, metadata.getForsendelseMottatt(), personoppslag.aktørId(metadata.getBrukersFnr()));
        var xmlDokument = DokumentEntitet.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(xml.getBytes(StandardCharsets.UTF_8), ArkivFilType.XML)
            .build();
        dokumentRepository.lagre(xmlDokument);
        return xmlDokument;
    }


    private String mapSøknadTilXML(SøknadDto søknad, LocalDateTime forsendelseMottatt, AktørId søker) {
        return switch (søknad) {
            case ForeldrepengesøknadDto fp -> fpMapper.tilXML(fp, forsendelseMottatt, søker);
            case EndringssøknadForeldrepengerDto endringFp -> fpMapper.tilXML(endringFp, forsendelseMottatt, søker);
            case EngangsstønadDto es -> esMapper.tilXML(es, forsendelseMottatt, søker);
            case SvangerskapspengesøknadDto svp -> svpMapper.tilXML(svp, forsendelseMottatt, søker);
        };
    }

}
