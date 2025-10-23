package no.nav.foreldrepenger.soknad.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import no.nav.foreldrepenger.soknad.innsending.kontrakt.BarnDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Målform;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Saksnummer;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;

public class EndringssøknadBuilder {
    private LocalDateTime mottattdato;
    private Saksnummer saksnummer;
    private SøkerDto søkerinfo;
    private BrukerRolle rolle;
    private Målform språkkode;
    private BarnDto barn;
    private AnnenForelderDto annenForelder;
    private UttaksplanDto uttaksplan;
    private List<VedleggDto> vedlegg;

    public EndringssøknadBuilder(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
        this.språkkode = Målform.standard();
    }

    public EndringssøknadBuilder medMottattdato(LocalDate mottattdato) {
        this.mottattdato = LocalDateTime.of(mottattdato, LocalTime.now());
        return this;
    }

    public EndringssøknadBuilder medMottattdato(LocalDateTime mottattdato) {
        this.mottattdato = mottattdato;
        return this;
    }

    public EndringssøknadBuilder medRolle(BrukerRolle rolle) {
        this.rolle = rolle;
        return this;
    }

    public EndringssøknadBuilder medSøkerinfo(SøkerDto søkerinfo) {
        this.søkerinfo = søkerinfo;
        return this;
    }

    public EndringssøknadBuilder medSpråkkode(Målform språkkode) {
        this.språkkode = språkkode;
        return this;
    }

    public EndringssøknadBuilder medBarn(BarnDto barn) {
        this.barn = barn;
        return this;
    }

    public EndringssøknadBuilder medAnnenForelder(AnnenForelderDto annenForelder) {
        this.annenForelder = annenForelder;
        return this;
    }

    public EndringssøknadBuilder medUttaksplan(UttaksplanDto uttaksplan) {
        this.uttaksplan = uttaksplan;
        return this;
    }

    public EndringssøknadBuilder medUttaksplan(List<Uttaksplanperiode> uttaksplanperiodeer) {
        this.uttaksplan = new UttaksplanDto(null, uttaksplanperiodeer);
        return this;
    }

    public EndringssøknadBuilder medVedlegg(List<VedleggDto> vedlegg) {
        this.vedlegg = vedlegg;
        return this;
    }

    public EndringssøknadForeldrepengerDto build() {
        if (mottattdato == null) {
            mottattdato = LocalDateTime.now();
        }
        return new EndringssøknadForeldrepengerDto(mottattdato, saksnummer, søkerinfo, rolle, språkkode, barn, annenForelder, uttaksplan , vedlegg);
    }
}
