package no.nav.foreldrepenger.soknad.server.error;

public enum FeilKode {

    IKKE_TILGANG("Ikke tilgang"),
    DUPLIKAT_FORSENDELSE("Duplikat forespørsel"),
    VEDLEGG_OPPLASTNING("Opplasting av vedlegg feilet"),
    KRYPTERING_MELLOMLAGRING("Kryptering eller dekryptering av mellomlagring feilet");


    private final String beskrivelse;

    FeilKode(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }
}
