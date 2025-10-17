package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;

class ArkivUtilTest {

    @ParameterizedTest
    @MethodSource("dokumenttypeTilSkjemaMapping")
    void skal_mappe_dokumenttype_til_riktig_skjemanummer(DokumentTypeId dokumenttype, NAVSkjema forventetSkjemanummer) {
        assertThat(ArkivUtil.mapDokumentTypeId(dokumenttype)).isEqualTo(forventetSkjemanummer);
    }

    @ParameterizedTest
    @MethodSource("dokumenttypeTilBehandlingstemaMapping")
    void skal_mappe_dokumenttype_til_riktig_behandlingstema(DokumentTypeId dokumenttype, BehandlingTema forventetBehandlingstema) {
        assertThat(ArkivUtil.behandlingstemaFraSøknadDokumentType(dokumenttype)).isEqualTo(forventetBehandlingstema);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> dokumenttypeTilSkjemaMapping() {
        return Stream.of(
            Arguments.of(DokumentTypeId.I000001, NAVSkjema.SKJEMA_SVANGERSKAPSPENGER),
            Arguments.of(DokumentTypeId.I000002, NAVSkjema.SKJEMA_FORELDREPENGER_ADOPSJON),
            Arguments.of(DokumentTypeId.I000003, NAVSkjema.SKJEMA_ENGANGSSTØNAD_FØDSEL),
            Arguments.of(DokumentTypeId.I000004, NAVSkjema.SKJEMA_ENGANGSSTØNAD_ADOPSJON),
            Arguments.of(DokumentTypeId.I000005, NAVSkjema.SKJEMA_FORELDREPENGER_FØDSEL),
            Arguments.of(DokumentTypeId.I000050, NAVSkjema.SKJEMA_FORELDREPENGER_ENDRING),
            Arguments.of(DokumentTypeId.I000060, NAVSkjema.SKJEMA_ANNEN_POST),
            Arguments.of(DokumentTypeId.I000037, NAVSkjema.UDEFINERT),
            Arguments.of(DokumentTypeId.I000038, NAVSkjema.UDEFINERT),
            Arguments.of(DokumentTypeId.I000039, NAVSkjema.UDEFINERT),
            Arguments.of(DokumentTypeId.I000042, NAVSkjema.UDEFINERT),
            Arguments.of(DokumentTypeId.I000044, NAVSkjema.UDEFINERT)
        );
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> dokumenttypeTilBehandlingstemaMapping() {
        return Stream.of(
            Arguments.of(DokumentTypeId.I000001, BehandlingTema.SVANGERSKAPSPENGER),
            Arguments.of(DokumentTypeId.I000002, BehandlingTema.FORELDREPENGER_ADOPSJON),
            Arguments.of(DokumentTypeId.I000005, BehandlingTema.FORELDREPENGER_FØDSEL),
            Arguments.of(DokumentTypeId.I000004, BehandlingTema.ENGANGSSTØNAD_ADOPSJON),
            Arguments.of(DokumentTypeId.I000003, BehandlingTema.ENGANGSSTØNAD_FØDSEL),
            Arguments.of(DokumentTypeId.I000050, BehandlingTema.FORELDREPENGER_ENDRING)
        );
    }


}
