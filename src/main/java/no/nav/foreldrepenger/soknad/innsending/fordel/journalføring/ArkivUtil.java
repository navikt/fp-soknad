package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_ENDRING;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.SVANGERSKAPSPENGER;

import java.util.Map;
import java.util.Set;

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

    private ArkivUtil() {
    }

    public static DokumentTypeId utledHovedDokumentType(Set<DokumentTypeId> alleTyper) {
        return MapNAVSkjemaDokumentTypeId.velgRangertHovedDokumentType(alleTyper);
    }

    public static BehandlingTema behandlingstemaFraSøknadDokumentType(DokumentTypeId type) {
        return DOKUMENT_BEHANDLING_TEMA.get(type);
    }
}
