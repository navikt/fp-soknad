package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.soknad.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UtsettelseÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;

/**
 * En endringssøknad inneholder hele uttaksplanen, som lagres i fp-oversikt. FPSAK, PDF og ruting skal bare ha perioder
 * fra og med endringsdato. En periode som krysser endringsdato, kuttes slik at den starter på endringsdato.
 *
 * FPSAK utleder endringsdato fra første innsendte periode. Starter ingen søkerperiode på endringsdato, for eksempel
 * ved utsatt oppstart, slettet periode eller når søker har fjernet sin del av samtidig uttak, legges det inn fri
 * utsettelse for søker fra endringsdato til dagen før neste søkerperiode. Finnes ingen senere søkerperiode, varer
 * den frie utsettelsen én dag.
 *
 * Uten endringsdato (eldre klienter) gjelder en overgangsordning, se {@link #utledEndringsdatoForEldreKlient}.
 */
public final class EndringsdatoFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EndringsdatoFilter.class);

    private EndringsdatoFilter() {
    }

    static List<UttakPeriodeDto> perioderFraEndringsdato(List<UttakPeriodeDto> perioder, LocalDate endringsdato, BrukerRolle søkerRolle) {
        var fraEndringsdato = perioder.stream()
            .filter(p -> !p.tom().isBefore(endringsdato))
            .map(p -> kuttVedEndringsdato(p, endringsdato))
            .sorted(Comparator.comparing(UttakPeriodeDto::fom))
            .toList();
        var nesteSøkerperiode = fraEndringsdato.stream()
            .filter(p -> p.søker() != null)
            .map(UttakPeriodeDto::fom)
            .min(LocalDate::compareTo);
        if (nesteSøkerperiode.filter(endringsdato::isEqual).isPresent()) {
            return fraEndringsdato;
        }
        var tom = nesteSøkerperiode.map(d -> d.minusDays(1)).orElse(endringsdato);
        var resultat = new ArrayList<UttakPeriodeDto>();
        resultat.add(friUtsettelse(endringsdato, tom, søkerRolle));
        resultat.addAll(fraEndringsdato);
        return List.copyOf(resultat);
    }

    public static UttaksplanDto uttaksplanFraEndringsdato(EndringssøknadForeldrepengerDto endringssøknad) {
        var uttaksplan = endringssøknad.uttaksplan();
        return new UttaksplanDto(uttaksplan.ønskerJustertUttakVedFødsel(),
            perioderFraEndringsdato(uttaksplan.perioder(), gjeldendeEndringsdato(endringssøknad), endringssøknad.rolle()));
    }

    /**
     * Søknaden med periodene som skal til FPSAK og PDF. En endringsdato som utledes for eldre klienter, legges ikke
     * inn i søknaden: {@code endringsdato()} er fortsatt null, slik at søknaden ikke sendes til fp-oversikt.
     */
    public static EndringssøknadForeldrepengerDto fraEndringsdato(EndringssøknadForeldrepengerDto e) {
        return new EndringssøknadForeldrepengerDto(e.mottattdato(), e.saksnummer(), e.søkerinfo(), e.rolle(), e.språkkode(), e.barn(),
            e.annenForelder(), uttaksplanFraEndringsdato(e), e.vedlegg(), e.endringsdato());
    }

    private static LocalDate gjeldendeEndringsdato(EndringssøknadForeldrepengerDto endringssøknad) {
        if (endringssøknad.endringsdato() != null) {
            return endringssøknad.endringsdato();
        }
        return utledEndringsdatoForEldreKlient(endringssøknad);
    }

    /**
     * OVERGANGSORDNING for eldre frontend som ikke sender endringsdato. Fjernes når alle klienter sender endringsdato,
     * sammen med overgangskoden i SendSøknadTilOversiktTask. Endringsdato bør da bli påkrevd for endringssøknad.
     *
     * Eldre frontend kutter planen ved sitt eget endringstidspunkt og sender bare perioder som starter på eller etter
     * det, eller som krysser det. Annen parts perioder sendte den bare når de var uttak som FPSAK fikk som opphold.
     * Første dag i den innsendte planen er derfor endringstidspunktet, eller starten på en periode som krysser det, og
     * brukes som endringsdato for FPSAK, ruting og PDF.
     *
     * Vanligvis starter søker en periode der, og FPSAK får det samme som før. Når søker har fjernet sin del av samtidig
     * uttak, starter planen med en periode der bare annen part har uttak. Den ble før sendt som opphold. Nå får søker
     * i stedet fri utsettelse fram til neste søkerperiode, slik at FPSAK ikke får for sen endringsdato.
     *
     * Planen er aldri tom her: UttaksperioderValidering krever minst én søkerperiode når endringsdato mangler.
     */
    private static LocalDate utledEndringsdatoForEldreKlient(EndringssøknadForeldrepengerDto endringssøknad) {
        return endringssøknad.uttaksplan().perioder().stream().map(UttakPeriodeDto::fom).min(LocalDate::compareTo)
            .orElseThrow(() -> new IllegalStateException("Endringssøknad uten endringsdato mangler perioder"));
    }

    private static UttakPeriodeDto kuttVedEndringsdato(UttakPeriodeDto periode, LocalDate endringsdato) {
        if (!periode.fom().isBefore(endringsdato)) {
            return periode;
        }
        LOG.info("{} {} - {} krysser endringsdato {}. Kuttes til å starte på endringsdato.",
            periode.søker() != null ? "Søkerperiode" : "Annen parts periode", periode.fom(), periode.tom(), endringsdato);
        return new UttakPeriodeDto(endringsdato, periode.tom(), periode.søker(), periode.annenPart(), periode.annenPartEøs());
    }

    private static UttakPeriodeDto friUtsettelse(LocalDate fom, LocalDate tom, BrukerRolle søkerRolle) {
        var forelder = switch (søkerRolle) {
            case MOR -> Rolle.MOR;
            case FAR, MEDMOR -> Rolle.FAR_MEDMOR;
        };
        var fri = new UttakDto(forelder, null, UtsettelseÅrsak.FRI, null, null, null, null, false, null);
        return new UttakPeriodeDto(fom, tom, fri, null, null);
    }
}
