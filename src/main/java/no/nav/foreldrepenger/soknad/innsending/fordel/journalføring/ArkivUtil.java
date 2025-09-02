package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentKategori;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema.UDEFINERT;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;


public final class ArkivUtil {

    private static final int UDEF_RANK = 99;

    private static final Map<DokumentTypeId, BehandlingTema> DOKUMENT_BEHANDLING_TEMA = Map.ofEntries(
        Map.entry(SØKNAD_SVANGERSKAPSPENGER, SVANGERSKAPSPENGER),
        Map.entry(SØKNAD_FORELDREPENGER_ADOPSJON, FORELDREPENGER_ADOPSJON),
        Map.entry(SØKNAD_FORELDREPENGER_FØDSEL, FORELDREPENGER_FØDSEL),
        Map.entry(SØKNAD_ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_ADOPSJON),
        Map.entry(SØKNAD_ENGANGSSTØNAD_FØDSEL, ENGANGSSTØNAD_FØDSEL),
        Map.entry(FLEKSIBELT_UTTAK_FORELDREPENGER, FORELDREPENGER),
        Map.entry(FORELDREPENGER_ENDRING_SØKNAD, FORELDREPENGER));

    private static final Map<BehandlingTema, Integer> BTEMA_RANK = Map.ofEntries(
        Map.entry(BehandlingTema.FORELDREPENGER_FØDSEL, 1),
        Map.entry(BehandlingTema.ENGANGSSTØNAD_FØDSEL, 2),
        Map.entry(BehandlingTema.FORELDREPENGER_ADOPSJON, 3),
        Map.entry(BehandlingTema.ENGANGSSTØNAD_ADOPSJON, 4),
        Map.entry(BehandlingTema.FORELDREPENGER, 5),
        Map.entry(BehandlingTema.ENGANGSSTØNAD, 6),
        Map.entry(BehandlingTema.SVANGERSKAPSPENGER, 7),
        Map.entry(UDEFINERT, UDEF_RANK));

    private static final Map<Integer, BehandlingTema> RANK_BTEMA = BTEMA_RANK.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));


    private ArkivUtil() {
    }

    public static DokumentKategori utledKategoriFraDokumentType(DokumentTypeId doktype) {
        if (DokumentTypeId.erSøknadType(doktype)) {
            return DokumentKategori.SØKNAD;
        }
        return DokumentKategori.IKKE_TOLKBART_SKJEMA;
    }

    public static BehandlingTema behandlingTemaFraDokumentType(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        int btRank = behandlingstemaRank(behandlingTema);
        int dtRank = behandlingstemaRank(mapDokumenttype(dokumentTypeId));

        return behandlingstemaFromRank(Math.min(btRank, dtRank));
    }

    public static BehandlingTema behandlingTemaFraDokumentTypeSet(BehandlingTema behandlingTema, Collection<DokumentTypeId> typer) {
        int btRank = behandlingstemaRank(behandlingTema);
        int dtRank = typer.stream()
            .map(ArkivUtil::mapDokumenttype)
            .map(ArkivUtil::behandlingstemaRank)
            .min(Comparator.naturalOrder())
            .orElseGet(() -> behandlingstemaRank(null));

        return behandlingstemaFromRank(Math.min(btRank, dtRank));
    }

    public static DokumentTypeId utledHovedDokumentType(Set<DokumentTypeId> alleTyper) {
        return MapNAVSkjemaDokumentTypeId.velgRangertHovedDokumentType(alleTyper);
    }

    private static BehandlingTema mapDokumenttype(DokumentTypeId type) {
        return Optional.ofNullable(type).map(DOKUMENT_BEHANDLING_TEMA::get).orElse(UDEFINERT);
    }

    private static int behandlingstemaRank(BehandlingTema bt) {
        return Optional.ofNullable(bt).map(BTEMA_RANK::get).orElse(UDEF_RANK);
    }

    private static BehandlingTema behandlingstemaFromRank(int rank) {
        return RANK_BTEMA.getOrDefault(rank, UDEFINERT);
    }

}
