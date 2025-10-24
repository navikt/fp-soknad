package no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper;

import java.util.List;
import java.util.UUID;

import no.nav.foreldrepenger.kontrakter.fpsoknad.svangerskapspenger.ArbeidsforholdDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.vedlegg.Dokumenterer;
import no.nav.foreldrepenger.kontrakter.fpsoknad.vedlegg.InnsendingType;
import no.nav.foreldrepenger.kontrakter.fpsoknad.vedlegg.VedleggDto;
import no.nav.foreldrepenger.kontrakter.fpsoknad.vedlegg.ÅpenPeriodeDto;


public class DokumentasjonReferanseMapper {

    private DokumentasjonReferanseMapper() {
    }

    public static List<UUID> dokumentasjonSomDokumentererBarn(List<VedleggDto> vedleggene) {
        return vedleggene.stream()
            .filter(vedlegg -> !erAutomatiskVedlegg(vedlegg))
            .filter(vedlegg -> vedlegg.dokumenterer().type().equals(Dokumenterer.DokumentererType.BARN))
            .map(VedleggDto::uuid)
            .toList();
    }

    public static List<UUID> dokumentasjonSomDokumentererOpptjeningsperiode(List<VedleggDto> vedleggene, ÅpenPeriodeDto periode) {
        return vedleggene.stream()
            .filter(vedlegg -> !erAutomatiskVedlegg(vedlegg))
            .filter(vedlegg -> vedlegg.dokumenterer().type().equals(Dokumenterer.DokumentererType.OPPTJENING))
            .filter(vedlegg -> vedlegg.dokumenterer().perioder().contains(periode))
            .map(VedleggDto::uuid)
            .toList();
    }

    public static List<UUID> dokumentasjonSomDokumentererUttaksperiode(List<VedleggDto> vedleggene, ÅpenPeriodeDto periode) {
        return vedleggene.stream()
            .filter(vedlegg -> !erAutomatiskVedlegg(vedlegg))
            .filter(vedlegg -> vedlegg.dokumenterer().type().equals(Dokumenterer.DokumentererType.UTTAK))
            .filter(vedlegg -> vedlegg.dokumenterer().perioder().contains(periode))
            .map(VedleggDto::uuid)
            .toList();
    }

    public static List<UUID> dokumentasjonSomDokumentererTilrettelegggingAv(List<VedleggDto> vedleggene,
                                                                                        ArbeidsforholdDto arbeidsforholdet) {
        return vedleggene.stream()
            .filter(vedlegg -> !erAutomatiskVedlegg(vedlegg))
            .filter(vedlegg -> vedlegg.dokumenterer().type().equals(Dokumenterer.DokumentererType.TILRETTELEGGING))
            .filter(vedleggDto -> matcherArbeidsforhold(vedleggDto.dokumenterer().arbeidsforhold(), arbeidsforholdet))
            .map(VedleggDto::uuid)
            .toList();
    }

    private static boolean matcherArbeidsforhold(ArbeidsforholdDto arbeidsforholdVedlegg, ArbeidsforholdDto arbeidsforholdSøknad) {
        return switch (arbeidsforholdSøknad) {
            case ArbeidsforholdDto.VirksomhetDto(var id1) -> arbeidsforholdVedlegg instanceof ArbeidsforholdDto.VirksomhetDto(var id2) && id2.equals(id1);
            case ArbeidsforholdDto.PrivatArbeidsgiverDto(var id1) -> arbeidsforholdVedlegg instanceof ArbeidsforholdDto.PrivatArbeidsgiverDto(var id2) && id2.equals(id1);
            case ArbeidsforholdDto.SelvstendigNæringsdrivendeDto _ -> arbeidsforholdVedlegg instanceof ArbeidsforholdDto.SelvstendigNæringsdrivendeDto;
            case ArbeidsforholdDto.FrilanserDto _ -> arbeidsforholdVedlegg instanceof ArbeidsforholdDto.FrilanserDto;
            default -> throw new IllegalStateException("Unexpected value: " + arbeidsforholdSøknad);
        };
    }

    private static boolean erAutomatiskVedlegg(VedleggDto vedlegg) {
        return InnsendingType.AUTOMATISK.equals(vedlegg.innsendingsType());
    }
}
