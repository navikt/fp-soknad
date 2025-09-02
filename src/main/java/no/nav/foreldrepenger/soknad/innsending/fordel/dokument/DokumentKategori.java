package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

public enum DokumentKategori {
    SØKNAD("SOK"),
    IKKE_TOLKBART_SKJEMA("IS"),
    ;

    private String offisiellKode;

    DokumentKategori(String offisiellKode) {
        this.offisiellKode = offisiellKode;
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }
}
