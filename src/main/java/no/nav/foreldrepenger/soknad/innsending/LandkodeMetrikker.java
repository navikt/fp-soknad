package no.nav.foreldrepenger.soknad.innsending;

import static no.nav.foreldrepenger.soknad.utils.StreamUtil.safeStream;
import static no.nav.vedtak.log.metrics.MetricsUtil.REGISTRY;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart.UtenlandskForelderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LandkodeMetrikker {
    private static final Logger LOG = LoggerFactory.getLogger(LandkodeMetrikker.class);
    private static final String METRIKK_NAVN = "fp.soknad.landkode.alpha2";

    private LandkodeMetrikker() {
    }

    static void registrer(SøknadDto søknad) {
        try {
            if (landkoder(søknad).filter(Objects::nonNull).anyMatch(LandkodeMetrikker::erAlpha2)) {
                REGISTRY.counter(METRIKK_NAVN, "soknadstype", søknadstype(søknad)).increment();
            }
        } catch (RuntimeException e) {
            LOG.info("Klarte ikke å registrere landkodemetrikk", e);
        }
    }

    private static Stream<String> landkoder(SøknadDto søknad) {
        return switch (søknad) {
            case ForeldrepengesøknadDto fp -> Stream.of(
                landkoderFraUtenlandsopphold(fp.utenlandsopphold()),
                safeStream(fp.andreInntekterSiste10Mnd()).map(inntekt -> inntekt.land()),
                Stream.ofNullable(fp.egenNæring()).map(næring -> næring.registrertILand()),
                landkodeFraAnnenForelder(fp.annenForelder())
            ).flatMap(stream -> stream);
            case SvangerskapspengesøknadDto svp -> Stream.of(
                landkoderFraUtenlandsopphold(svp.utenlandsopphold()),
                safeStream(svp.andreInntekterSiste10Mnd()).map(inntekt -> inntekt.land()),
                Stream.ofNullable(svp.egenNæring()).map(næring -> næring.registrertILand())
            ).flatMap(stream -> stream);
            case EngangsstønadDto es -> landkoderFraUtenlandsopphold(es.utenlandsopphold());
            case EndringssøknadForeldrepengerDto endring -> landkodeFraAnnenForelder(endring.annenForelder());
        };
    }

    private static Stream<String> landkoderFraUtenlandsopphold(List<? extends UtenlandsoppholdsperiodeDto> opphold) {
        return safeStream(opphold).map(UtenlandsoppholdsperiodeDto::landkode);
    }

    private static Stream<String> landkodeFraAnnenForelder(AnnenForelderDto annenForelder) {
        return annenForelder instanceof UtenlandskForelderDto utenlandsk ? Stream.of(utenlandsk.bostedsland()) : Stream.empty();
    }

    static boolean erAlpha2(String landkode) {
        return landkode.matches("(?i)[A-Z]{2}");
    }

    static String søknadstype(SøknadDto søknad) {
        return switch (søknad) {
            case ForeldrepengesøknadDto _, EndringssøknadForeldrepengerDto _ -> "FP";
            case SvangerskapspengesøknadDto _ -> "SVP";
            case EngangsstønadDto _ -> "ES";
        };
    }
}
