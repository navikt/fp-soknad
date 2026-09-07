package no.nav.foreldrepenger.soknad.kontrakt.validering;

import java.util.Locale;
import java.util.Set;

public final class Landkode {
    public static final String KOSOVO = "XXK";

    private static final Set<String> ALPHA3 = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3);

    private Landkode() {
    }

    public static String normaliser(String landkode) {
        if (landkode == null) {
            return null;
        }
        return landkode.toUpperCase(Locale.ROOT);
    }

    public static boolean erGyldig(String landkode) {
        if (landkode == null || !landkode.matches("[A-Za-z]{3}")) {
            return false;
        }
        var normalisert = normaliser(landkode);
        return KOSOVO.equals(normalisert) || ALPHA3.contains(normalisert);
    }
}
