package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class MapNAVSkjemaDokumentTypeId {

    private static final int MAX_RANK = 99;

    private MapNAVSkjemaDokumentTypeId() {
        // Hide constructor
    }

    private static final Map<DokumentTypeId, NAVSkjema> DOKUMENT_TYPE_BREVKODE = Map.ofEntries(
        Map.entry(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER, NAVSkjema.SKJEMA_SVANGERSKAPSPENGER),
        Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON, NAVSkjema.SKJEMA_FORELDREPENGER_ADOPSJON),
        Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, NAVSkjema.SKJEMA_FORELDREPENGER_FØDSEL),
        Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON, NAVSkjema.SKJEMA_ENGANGSSTØNAD_ADOPSJON),
        Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, NAVSkjema.SKJEMA_ENGANGSSTØNAD_FØDSEL),
        Map.entry(DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER, NAVSkjema.SKJEMA_FLEKSIBELT_UTTAK),
        Map.entry(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, NAVSkjema.SKJEMA_FORELDREPENGER_ENDRING),
        Map.entry(DokumentTypeId.ANNET, NAVSkjema.SKJEMA_ANNEN_POST),
        Map.entry(DokumentTypeId.UDEFINERT, NAVSkjema.UDEFINERT));

    private static final Map<DokumentTypeId, Integer> DOKUMENT_TYPE_RANK = Map.ofEntries(
        Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, 2),
        Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, 3),
        Map.entry(DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER, 4),
        Map.entry(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER, 5),
        Map.entry(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, 6),
        Map.entry(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON, 7),
        Map.entry(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON, 8),
        Map.entry(DokumentTypeId.TILBAKEBETALING_UTTALSELSE, 11),
        Map.entry(DokumentTypeId.TILBAKEKREV_UTTALELSE, 12));

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
            .filter(t -> !DokumentTypeId.ANNET.equals(t))
            .collect(Collectors.toSet());
        var minrank = typerMedBeskrivelse.stream()
            .map(MapNAVSkjemaDokumentTypeId::dokumentTypeRank)
            .min(Comparator.naturalOrder()).orElse(MAX_RANK);
        if (minrank < MAX_RANK) {
            return RANK_DOKUMENT_TYPE.get(minrank);
        } else {
            return typerMedBeskrivelse.stream().findFirst().or(() -> alleTyper.stream().findFirst()).orElse(DokumentTypeId.UDEFINERT);
        }
    }

    private static int dokumentTypeRank(DokumentTypeId dokumentTypeId) {
        return DOKUMENT_TYPE_RANK.getOrDefault(dokumentTypeId, MAX_RANK);
    }
}
