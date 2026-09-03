package no.nav.foreldrepenger.soknad.kontrakt.validering;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Landkode {
    private static final String KOSOVO_ALPHA2 = "XK";
    public static final String KOSOVO = "XXK";

    private static final Map<String, String> ALPHA2_TIL_ALPHA3 = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
        .collect(Collectors.toUnmodifiableMap(landkode -> landkode, landkode -> Locale.of("", landkode).getISO3Country()));
    private static final Set<String> ALPHA3 = Set.copyOf(ALPHA2_TIL_ALPHA3.values());

    private Landkode() {
    }

    public static String normaliser(String landkode) {
        if (landkode == null) {
            return null;
        }
        var normalisert = landkode.toUpperCase(Locale.ROOT);
        if (KOSOVO_ALPHA2.equals(normalisert) || KOSOVO.equals(normalisert)) {
            return KOSOVO;
        }
        return ALPHA2_TIL_ALPHA3.getOrDefault(normalisert, normalisert);
    }

    public static boolean erGyldig(String landkode) {
        if (landkode == null || !landkode.matches("[A-Za-z]{2,3}")) {
            return false;
        }
        var normalisert = normaliser(landkode);
        return KOSOVO.equals(normalisert) || ALPHA3.contains(normalisert);
    }
}
