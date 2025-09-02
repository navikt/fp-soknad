package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import java.util.Set;

public enum ArkivFilType {
    PDFA,
    XML,
    JSON;

    private static final Set<ArkivFilType> KLARTEKST = Set.of(XML, JSON);

    public static boolean erKlartekstType(ArkivFilType arkivFilType) {
        return KLARTEKST.contains(arkivFilType);
    }

}
