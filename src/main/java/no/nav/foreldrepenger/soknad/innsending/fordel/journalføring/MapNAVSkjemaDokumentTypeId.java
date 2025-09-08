package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;

public class MapNAVSkjemaDokumentTypeId {

    private static final int MAX_RANK = 99;

    private MapNAVSkjemaDokumentTypeId() {
        // Hide constructor
    }

    private static final Map<DokumentTypeId, NAVSkjema> DOKUMENT_TYPE_BREVKODE = Map.ofEntries(
        Map.entry(DokumentTypeId.I000001, NAVSkjema.SKJEMA_SVANGERSKAPSPENGER),
        Map.entry(DokumentTypeId.I000002, NAVSkjema.SKJEMA_FORELDREPENGER_ADOPSJON),
        Map.entry(DokumentTypeId.I000003, NAVSkjema.SKJEMA_ENGANGSSTØNAD_FØDSEL),
        Map.entry(DokumentTypeId.I000004, NAVSkjema.SKJEMA_ENGANGSSTØNAD_ADOPSJON),
        Map.entry(DokumentTypeId.I000005, NAVSkjema.SKJEMA_FORELDREPENGER_FØDSEL),
        Map.entry(DokumentTypeId.I000050, NAVSkjema.SKJEMA_FORELDREPENGER_ENDRING),
        Map.entry(DokumentTypeId.I000060, NAVSkjema.SKJEMA_ANNEN_POST));

    private static final Map<DokumentTypeId, Integer> DOKUMENT_TYPE_RANK = Map.ofEntries(
        Map.entry(DokumentTypeId.I000005, 1),
        Map.entry(DokumentTypeId.I000003, 2),
        Map.entry(DokumentTypeId.I000001, 3),
        Map.entry(DokumentTypeId.I000050, 4),
        Map.entry(DokumentTypeId.I000002, 5),
        Map.entry(DokumentTypeId.I000004, 6),
        Map.entry(DokumentTypeId.I000114, 7),
        Map.entry(DokumentTypeId.I000119, 8));

    private static final Map<Integer, DokumentTypeId> RANK_DOKUMENT_TYPE = DOKUMENT_TYPE_RANK.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public static NAVSkjema mapDokumentTypeId(DokumentTypeId typeId) {
        if (typeId == null) {
            return NAVSkjema.UDEFINERT;
        }
        return DOKUMENT_TYPE_BREVKODE.getOrDefault(typeId, NAVSkjema.UDEFINERT);
    }

    public static DokumentTypeId velgRangertHovedDokumentType(Collection<DokumentTypeId> alleTyper) {
        var typerMedBeskrivelse = alleTyper.stream()
            .filter(t -> !DokumentTypeId.I000060.equals(t))
            .collect(Collectors.toSet());
        var minrank = typerMedBeskrivelse.stream()
            .map(MapNAVSkjemaDokumentTypeId::dokumentTypeRank)
            .min(Comparator.naturalOrder()).orElse(MAX_RANK);
        if (minrank < MAX_RANK) {
            return RANK_DOKUMENT_TYPE.get(minrank);
        } else {
            return typerMedBeskrivelse.stream().findFirst().or(() -> alleTyper.stream().findFirst()).orElseThrow();
        }
    }

    private static int dokumentTypeRank(DokumentTypeId dokumentTypeId) {
        return DOKUMENT_TYPE_RANK.getOrDefault(dokumentTypeId, MAX_RANK);
    }
}
