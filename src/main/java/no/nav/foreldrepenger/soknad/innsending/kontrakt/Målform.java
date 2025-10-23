package no.nav.foreldrepenger.soknad.innsending.kontrakt;

public enum Målform {
    NB,
    NN,
    EN,
    E;

    public static Målform standard() {
        return NB;
    }
}
