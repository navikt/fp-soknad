package no.nav.foreldrepenger.soknad.kontrakt;

public enum Målform {
    NB,
    NN,
    EN,
    E;

    public static Målform standard() {
        return NB;
    }
}
