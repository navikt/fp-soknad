package no.nav.foreldrepenger.soknad.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import no.nav.foreldrepenger.soknad.innsending.kontrakt.BarnDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Målform;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;


public class EngangsstønadBuilder {
    private LocalDateTime mottattdato;
    private SøkerDto søkerinfo;
    private Målform språkkode;
    private BarnDto barn;
    private List<UtenlandsoppholdsperiodeDto> utenlandsopphold;
    private List<VedleggDto> vedlegg;

    public EngangsstønadBuilder() {
        this.språkkode = Målform.standard();
    }

    public EngangsstønadBuilder medMottattdato(LocalDate mottattdato) {
        this.mottattdato = LocalDateTime.of(mottattdato, LocalTime.now());
        return this;
    }

    public EngangsstønadBuilder medMottattdato(LocalDateTime mottattdato) {
        this.mottattdato = mottattdato;
        return this;
    }

    public EngangsstønadBuilder medSøkerinfo(SøkerDto søkerinfo) {
        this.søkerinfo = søkerinfo;
        return this;
    }

    public EngangsstønadBuilder medSpråkkode(Målform språkkode) {
        this.språkkode = språkkode;
        return this;
    }

    public EngangsstønadBuilder medBarn(BarnDto barn) {
        this.barn = barn;
        return this;
    }

    public EngangsstønadBuilder medUtenlandsopphold(List<UtenlandsoppholdsperiodeDto> utenlandsopphold) {
        this.utenlandsopphold = utenlandsopphold;
        return this;
    }

    public EngangsstønadBuilder medVedlegg(List<VedleggDto> vedlegg) {
        this.vedlegg = vedlegg;
        return this;
    }

    public EngangsstønadDto build() {
        if (mottattdato == null) {
            mottattdato = LocalDateTime.now();
        }
        return new EngangsstønadDto(mottattdato, søkerinfo, språkkode, barn, utenlandsopphold, vedlegg);
    }
}
