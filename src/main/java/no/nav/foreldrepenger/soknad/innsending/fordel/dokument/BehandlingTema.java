package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import java.util.LinkedHashMap;
import java.util.Map;


public enum BehandlingTema {
    ENGANGSSTØNAD("ab0327"),
    ENGANGSSTØNAD_FØDSEL("ab0050"),
    ENGANGSSTØNAD_ADOPSJON("ab0027"),
    FORELDREPENGER_FØDSEL("ab0047"),
    FORELDREPENGER_ADOPSJON("ab0072"),
    FORELDREPENGER_ENDRING("ab0326"),
    SVANGERSKAPSPENGER("ab0126");

    private static final Map<String, BehandlingTema> OFFISIELLE_KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (v.offisiellKode != null) {
                OFFISIELLE_KODER.putIfAbsent(v.offisiellKode, v);
            }
        }
    }

    private String offisiellKode;

    BehandlingTema(String offisiellKode) {
        this.offisiellKode = offisiellKode;
    }


    public static BehandlingTema fraOffisiellKode(String kode) {
        return OFFISIELLE_KODER.get(kode);
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }

}
