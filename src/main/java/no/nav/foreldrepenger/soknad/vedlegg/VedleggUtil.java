package no.nav.foreldrepenger.soknad.vedlegg;


import java.math.BigDecimal;
import java.math.RoundingMode;


public final class VedleggUtil {
    private final static BigDecimal BYTES_TO_KILOBYTES = BigDecimal.valueOf(1024L * 1024L);
    private final static BigDecimal BYTES_TO_MEGABYTES = BigDecimal.valueOf(1024L * 1024L);

    private VedleggUtil() {
    }

    public static BigDecimal megabytes(long bytes) {
        return BigDecimal.valueOf(bytes)
            .divide(BYTES_TO_MEGABYTES, 3, RoundingMode.HALF_UP);
    }
}
