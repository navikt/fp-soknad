package no.nav.foreldrepenger.soknad.kontrakt.builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import no.nav.foreldrepenger.soknad.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.Målform;
import no.nav.foreldrepenger.soknad.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.BarnDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.AnnenInntektDto;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.FrilansDto;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.NæringDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.VedleggDto;

public class ForeldrepengerBuilder implements SøknadBuilder<EngangsstønadBuilder> {
    private LocalDateTime mottattdato;
    private SøkerDto søkerinfo;
    private BrukerRolle rolle;
    private Målform språkkode;
    private BarnDto barn;
    private FrilansDto frilans;
    private NæringDto egenNæring;
    private List<AnnenInntektDto> andreInntekterSiste10Mnd;
    private AnnenForelderDto annenForelder;
    private Dekningsgrad dekningsgrad;
    private UttaksplanDto uttaksplan;
    private List<UtenlandsoppholdsperiodeDto> utenlandsopphold;
    private List<VedleggDto> vedlegg;

    public ForeldrepengerBuilder() {
        this.språkkode = Målform.standard();
    }

    public ForeldrepengerBuilder medMottattdato(LocalDate mottattdato) {
        this.mottattdato = LocalDateTime.of(mottattdato, LocalTime.now());
        return this;
    }

    public ForeldrepengerBuilder medMottattdato(LocalDateTime mottattdato) {
        this.mottattdato = mottattdato;
        return this;
    }

    public ForeldrepengerBuilder medRolle(BrukerRolle rolle) {
        this.rolle = rolle;
        return this;
    }

    public ForeldrepengerBuilder medSøkerinfo(SøkerDto søkerinfo) {
        this.søkerinfo = søkerinfo;
        return this;
    }

    public ForeldrepengerBuilder medSpråkkode(Målform språkkode) {
        this.språkkode = språkkode;
        return this;
    }

    public ForeldrepengerBuilder medFrilansInformasjon(FrilansDto frilans) {
        this.frilans = frilans;
        return this;
    }

    public ForeldrepengerBuilder medSelvstendigNæringsdrivendeInformasjon(NæringDto egenNæring) {
        this.egenNæring = egenNæring;
        return this;
    }

    public ForeldrepengerBuilder medAndreInntekterSiste10Mnd(List<AnnenInntektDto> andreInntekterSiste10Mnd) {
        this.andreInntekterSiste10Mnd = andreInntekterSiste10Mnd;
        return this;
    }

    public ForeldrepengerBuilder medBarn(BarnDto barn) {
        this.barn = barn;
        return this;
    }

    public ForeldrepengerBuilder medAnnenForelder(AnnenForelderDto annenForelder) {
        this.annenForelder = annenForelder;
        return this;
    }

    public ForeldrepengerBuilder medDekningsgrad(Dekningsgrad dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
        return this;
    }

    public ForeldrepengerBuilder medUtenlandsopphold(List<UtenlandsoppholdsperiodeDto> utenlandsopphold) {
        this.utenlandsopphold = utenlandsopphold;
        return this;
    }

    public ForeldrepengerBuilder medUttaksplan(UttaksplanDto uttaksplan) {
        this.uttaksplan = uttaksplan;
        return this;
    }

    public ForeldrepengerBuilder medUttaksplan(List<Uttaksplanperiode> uttaksperioder) {
        this.uttaksplan = new UttaksplanDto(null, uttaksperioder);
        return this;
    }

    public ForeldrepengerBuilder medVedlegg(List<VedleggDto> vedlegg) {
        this.vedlegg = vedlegg;
        return this;
    }

    public SøknadDto build() {
        if (mottattdato == null) {
            mottattdato = LocalDateTime.now();
        }
        return new ForeldrepengesøknadDto(
                mottattdato,
                søkerinfo,
                rolle,
                språkkode,
                barn,
                frilans,
                egenNæring,
                andreInntekterSiste10Mnd,
                annenForelder,
                dekningsgrad,
                uttaksplan,
                utenlandsopphold,
                vedlegg
        );
    }
}
