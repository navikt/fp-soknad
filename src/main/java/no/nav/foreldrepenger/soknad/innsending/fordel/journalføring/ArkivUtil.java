package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_ENDRING;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.SVANGERSKAPSPENGER;

import java.util.Map;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;


public final class ArkivUtil {

    private static final Map<DokumentTypeId, BehandlingTema> DOKUMENT_BEHANDLING_TEMA = Map.ofEntries(
        Map.entry(DokumentTypeId.I000001, SVANGERSKAPSPENGER),
        Map.entry(DokumentTypeId.I000002, FORELDREPENGER_ADOPSJON),
        Map.entry(DokumentTypeId.I000005, FORELDREPENGER_FØDSEL),
        Map.entry(DokumentTypeId.I000004, ENGANGSSTØNAD_ADOPSJON),
        Map.entry(DokumentTypeId.I000003, ENGANGSSTØNAD_FØDSEL),
        Map.entry(DokumentTypeId.I000050, FORELDREPENGER_ENDRING));

    private static final Map<DokumentTypeId, NAVSkjema> DOKUMENT_TYPE_BREVKODE = Map.ofEntries(
        Map.entry(DokumentTypeId.I000001, NAVSkjema.SKJEMA_SVANGERSKAPSPENGER),
        Map.entry(DokumentTypeId.I000002, NAVSkjema.SKJEMA_FORELDREPENGER_ADOPSJON),
        Map.entry(DokumentTypeId.I000003, NAVSkjema.SKJEMA_ENGANGSSTØNAD_FØDSEL),
        Map.entry(DokumentTypeId.I000004, NAVSkjema.SKJEMA_ENGANGSSTØNAD_ADOPSJON),
        Map.entry(DokumentTypeId.I000005, NAVSkjema.SKJEMA_FORELDREPENGER_FØDSEL),
        Map.entry(DokumentTypeId.I000050, NAVSkjema.SKJEMA_FORELDREPENGER_ENDRING),
        Map.entry(DokumentTypeId.I000060, NAVSkjema.SKJEMA_ANNEN_POST));

    private ArkivUtil() {
        // Hide constructor
    }

    public static NAVSkjema mapDokumentTypeId(DokumentTypeId typeId) {
        return DOKUMENT_TYPE_BREVKODE.getOrDefault(typeId, NAVSkjema.UDEFINERT);
    }

    public static BehandlingTema behandlingstemaFraSøknadDokumentType(DokumentTypeId type) {
        return DOKUMENT_BEHANDLING_TEMA.get(type);
    }
}
