package no.nav.foreldrepenger.soknad.innsending.fordel.xml;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V1SvangerskapspengerDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3EngangsstønadDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3ForeldrepengerDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;

import java.nio.charset.StandardCharsets;

import static no.nav.foreldrepenger.common.mapper.DefaultJsonMapper.MAPPER;

@ApplicationScoped
public class StrukturertDokumentMapperXML {

    private V3ForeldrepengerDomainMapper fpMapper;
    private V1SvangerskapspengerDomainMapper svpMapper;
    private V3EngangsstønadDomainMapper esMapper;

    public StrukturertDokumentMapperXML() {
        // CDI
    }

    @Inject
    public StrukturertDokumentMapperXML(V3ForeldrepengerDomainMapper fpMapper, V1SvangerskapspengerDomainMapper svpMapper, V3EngangsstønadDomainMapper esMapper) {
        this.fpMapper = fpMapper;
        this.svpMapper = svpMapper;
        this.esMapper = esMapper;
    }


    public Dokument lagStrukturertDokumentForArkivering(Dokument søknad, DokumentMetadata metadata) {
        var søknadJson = MAPPER.convertValue(søknad, SøknadDto.class);
        var xml = mapSøknadTilXML(søknadJson, new AktørId(metadata.getBrukerId()));
        return Dokument.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setErSøknad(true)
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(xml.getBytes(StandardCharsets.UTF_8), ArkivFilType.XML)
            .build();
        // TODO: Lagre ned dokument?
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
