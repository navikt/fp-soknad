package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum DokumentTypeId {

    // Søknader
    SØKNAD_SVANGERSKAPSPENGER("I000001", "Søknad om svangerskapspenger"),
    SØKNAD_FORELDREPENGER_ADOPSJON("I000002", "Søknad om foreldrepenger ved adopsjon"),
    SØKNAD_ENGANGSSTØNAD_FØDSEL("I000003", "Søknad om engangsstønad ved fødsel"),
    SØKNAD_ENGANGSSTØNAD_ADOPSJON("I000004", "Søknad om engangsstønad ved adopsjon"),
    SØKNAD_FORELDREPENGER_FØDSEL("I000005", "Søknad om foreldrepenger ved fødsel"),
    FLEKSIBELT_UTTAK_FORELDREPENGER("I000006", "Utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)"),
    FORELDREPENGER_ENDRING_SØKNAD("I000050", "Søknad om endring av uttak av foreldrepenger eller overføring av kvote"),

    // Vedlegg
    INNTEKTSOPPLYSNING_SELVSTENDIG("I000007", "Inntektsopplysninger om selvstendig næringsdrivende og/eller frilansere som skal ha foreldrepenger eller svangerskapspenger"),
    LEGEERKLÆRING("I000023", "Legeerklæring"),
    RESULTATREGNSKAP("I000032", "Resultatregnskap"),
    DOK_FERIE("I000036", "Dokumentasjon av ferie"),
    DOK_INNLEGGELSE("I000037", "Dokumentasjon av innleggelse i helseinstitusjon"),
    DOK_MORS_UTDANNING_ARBEID_SYKDOM("I000038", "Dokumentasjon av mors utdanning arbeid eller sykdom"),
    DOK_MILITÆR_SIVIL_TJENESTE("I000039", "Dokumentasjon av militær- eller siviltjeneste"),
    DOKUMENTASJON_AV_OMSORGSOVERTAKELSE("I000042", "Dokumentasjon av dato for overtakelse av omsorg"),
    DOK_ETTERLØNN("I000044", "Dokumentasjon av etterlønn/sluttvederlag"),
    BESKRIVELSE_FUNKSJONSNEDSETTELSE("I000045", "Beskrivelse av funksjonsnedsettelse"),
    BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM("I000051", "Bekreftelse på deltakelse i kvalifiseringsprogrammet"),
    BEKREFTELSE_FRA_STUDIESTED("I000061", "Bekreftelse fra studiested/skole"),
    BEKREFTELSE_VENTET_FØDSELSDATO("I000062", "Bekreftelse på ventet fødselsdato"),
    FØDSELSATTEST("I000063", "Fødselsattest"),
    KOPI_SKATTEMELDING("I000066", "Kopi av likningsattest eller selvangivelse"),
    BEKREFTELSE_FRA_ARBEIDSGIVER("I000065", "Bekreftelse fra arbeidsgiver"),
    SKJEMA_TILRETTELEGGING_OMPLASSERING("I000109", "Skjema for tilrettelegging og omplassering ved graviditet"),
    DOKUMENTASJON_ALENEOMSORG("I000110", "Dokumentasjon av aleneomsorg"),
    BEGRUNNELSE_SØKNAD_ETTERSKUDD("I000111", "Dokumentasjon av begrunnelse for hvorfor man søker tilbake i tid"),
    DOKUMENTASJON_INTRODUKSJONSPROGRAM("I000112", "Dokumentasjon av deltakelse i introduksjonsprogrammet"),
    TILBAKEKREV_UTTALELSE("I000114", "Uttalelse tilbakekreving"),
    DOKUMENTASJON_FORSVARSTJENESTE("I000116", "Bekreftelse på øvelse eller tjeneste i Forsvaret eller Sivilforsvaret"),
    DOKUMENTASJON_NAVTILTAK("I000117", "Bekreftelse på tiltak i regi av Arbeids- og velferdsetaten"),
    SEN_SØKNAD("I000118", "Begrunnelse for sen søknad"),
    TILBAKEBETALING_UTTALSELSE("I000119", "Uttalelse om tilbakebetaling"),
    MOR_INNLAGT("I000120", "Dokumentasjon på at mor er innlagt på sykehus"),
    MOR_SYK("I000121", "Dokumentasjon på at mor er syk"),
    FAR_INNLAGT("I000122", "Dokumentasjon på at far/medmor er innlagt på sykehus"),
    FAR_SYK("I000123", "Dokumentasjon på at far/medmor er syk"),
    BARN_INNLAGT("I000124", "Dokumentasjon på at barnet er innlagt på sykehus"),
    MOR_ARBEID_STUDIE("I000130", "Dokumentasjon på at mor studerer og arbeider til sammen heltid"),
    MOR_STUDIE("I000131", "Dokumentasjon på at mor studerer på heltid"),
    MOR_ARBEID("I000132", "Dokumentasjon på at mor er i arbeid"),
    MOR_KVALPROG("I000133", "Dokumentasjon av mors deltakelse i kvalifiseringsprogrammet"),
    SKATTEMELDING("I000140", "Skattemelding"),
    TERMINBEKREFTELSE("I000141", "Terminbekreftelse"),
    OPPHOLD("I000143", "Dokumentasjon på oppholdstillatelse"),
    REISE("I000144", "Dokumentasjon på reiser til og fra Norge"),
    OPPFØLGING("I000145", "Dokumentasjon på oppfølging i svangerskapet"),
    DOKUMENTASJON_INNTEKT("I000146", "Dokumentasjon på inntekt"),

    ANNET("I000060", "Annet"),
    UDEFINERT("-", "Ukjent type dokument");

    private static final Map<String, DokumentTypeId> OFFISIELLE_KODER = new LinkedHashMap<>();
    private static final Set<DokumentTypeId> SØKNAD_TYPER = Set.of(SØKNAD_ENGANGSSTØNAD_FØDSEL, SØKNAD_FORELDREPENGER_FØDSEL, SØKNAD_ENGANGSSTØNAD_ADOPSJON, SØKNAD_FORELDREPENGER_ADOPSJON, SØKNAD_SVANGERSKAPSPENGER);
    private static final Set<DokumentTypeId> ENDRING_SØKNAD_TYPER = Set.of(FORELDREPENGER_ENDRING_SØKNAD, FLEKSIBELT_UTTAK_FORELDREPENGER);

    static {
        for (var v : values()) {
            if (v.offisiellKode != null) {
                OFFISIELLE_KODER.putIfAbsent(v.offisiellKode, v);
            }
        }
    }

    @JsonValue
    private String offisiellKode;
    private String termnavn;

    DokumentTypeId(String offisiellKode, String termnavn) {
        this.offisiellKode = offisiellKode;
        this.termnavn = termnavn;
    }

    public static DokumentTypeId fraOffisiellKode(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return OFFISIELLE_KODER.getOrDefault(kode, UDEFINERT);
    }

    public static boolean erSøknadType(DokumentTypeId kode) {
        return SØKNAD_TYPER.contains(kode) || ENDRING_SØKNAD_TYPER.contains(kode);
    }

    public static boolean erFørsteSøknadType(DokumentTypeId kode) {
        return SØKNAD_TYPER.contains(kode);
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }

    public String getTermNavn() {
        return termnavn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<DokumentTypeId, String> {
        @Override
        public String convertToDatabaseColumn(DokumentTypeId attribute) {
            return attribute == null ? null : attribute.getOffisiellKode();
        }

        @Override
        public DokumentTypeId convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraOffisiellKode(dbData);
        }
    }

}
