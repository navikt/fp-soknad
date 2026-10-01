package no.nav.foreldrepenger.soknad.kontrakt.builder;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType;
import no.nav.foreldrepenger.kontrakter.felles.kodeverk.MorsAktivitet;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.EøsUttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Gradering;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.OverføringÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.SamtidigUttak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UtsettelseÅrsak;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.VedtattResultat;

/**
 * Brukes også i autotest
 */
public final class UttakPeriodeBuilder {

    private UttakPeriodeBuilder() {
    }

    public static PeriodeBuilder periode(LocalDate fom, LocalDate tom) {
        return new PeriodeBuilder(fom, tom);
    }

    public static UttakBuilder uttak(Rolle forelder) {
        return new UttakBuilder(forelder);
    }

    public static final class PeriodeBuilder {
        private final LocalDate fom;
        private final LocalDate tom;
        private UttakDto søker;
        private UttakDto annenPart;
        private EøsUttakDto annenPartEøs;

        private PeriodeBuilder(LocalDate fom, LocalDate tom) {
            this.fom = fom;
            this.tom = tom;
        }

        public PeriodeBuilder medSøker(UttakDto søker) {
            this.søker = søker;
            return this;
        }

        public PeriodeBuilder medAnnenPart(UttakDto annenPart) {
            this.annenPart = annenPart;
            return this;
        }

        public PeriodeBuilder medAnnenPartEøs(EøsUttakDto annenPartEøs) {
            this.annenPartEøs = annenPartEøs;
            return this;
        }

        public UttakPeriodeDto build() {
            return new UttakPeriodeDto(fom, tom, søker, annenPart, annenPartEøs);
        }
    }

    public static final class UttakBuilder {
        private final Rolle forelder;
        private KontoType kontoType;
        private UtsettelseÅrsak utsettelseÅrsak;
        private OverføringÅrsak overføringÅrsak;
        private Gradering gradering;
        private MorsAktivitet morsAktivitet;
        private SamtidigUttak samtidigUttak;
        private boolean flerbarnsdager;
        private VedtattResultat resultat;

        private UttakBuilder(Rolle forelder) {
            this.forelder = forelder;
        }

        public UttakBuilder medKontoType(KontoType kontoType) {
            this.kontoType = kontoType;
            return this;
        }

        public UttakBuilder medUtsettelseÅrsak(UtsettelseÅrsak utsettelseÅrsak) {
            this.utsettelseÅrsak = utsettelseÅrsak;
            return this;
        }

        public UttakBuilder medOverføringÅrsak(OverføringÅrsak overføringÅrsak) {
            this.overføringÅrsak = overføringÅrsak;
            return this;
        }

        public UttakBuilder medGradering(Gradering gradering) {
            this.gradering = gradering;
            return this;
        }

        public UttakBuilder medMorsAktivitet(MorsAktivitet morsAktivitet) {
            this.morsAktivitet = morsAktivitet;
            return this;
        }

        public UttakBuilder medSamtidigUttak(SamtidigUttak samtidigUttak) {
            this.samtidigUttak = samtidigUttak;
            return this;
        }

        public UttakBuilder medSamtidigUttak(BigDecimal samtidigUttakProsent) {
            return medSamtidigUttak(samtidigUttakProsent != null ? new SamtidigUttak(samtidigUttakProsent) : null);
        }

        public UttakBuilder medFlerbarnsdager(boolean flerbarnsdager) {
            this.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public UttakBuilder medResultat(VedtattResultat resultat) {
            this.resultat = resultat;
            return this;
        }

        public UttakDto build() {
            return new UttakDto(forelder, kontoType, utsettelseÅrsak, overføringÅrsak,
                    gradering, morsAktivitet, samtidigUttak, flerbarnsdager, resultat);
        }
    }
}
