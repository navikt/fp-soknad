package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

public enum NAVSkjema {

    SKJEMA_SVANGERSKAPSPENGER("NAV 14-04.10"),
    SKJEMA_FORELDREPENGER_ADOPSJON("NAV 14-05.06"),
    SKJEMA_ENGANGSSTØNAD_FØDSEL("NAV 14-05.07"),
    SKJEMA_ENGANGSSTØNAD_ADOPSJON("NAV 14-05.08"),
    SKJEMA_FORELDREPENGER_FØDSEL("NAV 14-05.09"),
    SKJEMA_FLEKSIBELT_UTTAK("NAV 14-16.05"),
    SKJEMA_FORELDREPENGER_ENDRING("NAV 14-05.10"),
    SKJEMA_ANNEN_POST("NAV 00-03.00"),
    UDEFINERT(null);

    private String offisiellKode;

    NAVSkjema(String offisiellKode) {
        this.offisiellKode = offisiellKode;
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }
}
