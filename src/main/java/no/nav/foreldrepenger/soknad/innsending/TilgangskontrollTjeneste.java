package no.nav.foreldrepenger.soknad.innsending;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.foreldrepenger.common.domain.Saksnummer;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.PdlKlientSystem;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;

@ApplicationScoped
public class TilgangskontrollTjeneste {

    private InnloggetBruker innloggetBruker;
    private FpsakTjeneste fpsakTjeneste;
    private PdlKlientSystem pdlKlientSystem;

    public TilgangskontrollTjeneste() {
        // CDI
    }

    @Inject
    public TilgangskontrollTjeneste(InnloggetBruker innloggetBruker, FpsakTjeneste fpsakTjeneste, PdlKlientSystem pdlKlientSystem) {
        this.innloggetBruker = innloggetBruker;
        this.fpsakTjeneste = fpsakTjeneste;
        this.pdlKlientSystem = pdlKlientSystem;
    }


    protected void validerSøkerFraKontekstErSammeSomSøknad(Fødselsnummer fnrFraSøknad) {
        if (!innloggetBruker.brukerFraKontekst().equals(fnrFraSøknad.value())) {
            // TODO: Funksjonell feilmelding til bruker!
            throw new IllegalStateException("Fødselsnummer i søknad matcher ikke innlogget bruker. Forekommer typisk når søker og annenpart søker på samme maskin.");
        }
    }

    protected void validerSaksnummerKnyttetTilSøker(Saksnummer saksnummer) {
        var fagsakinformasjon = fpsakTjeneste.finnFagsakInfomasjon(new SaksnummerDto(saksnummer.value()));
        var aktøridSøker = pdlKlientSystem.aktørId(innloggetBruker.brukerFraKontekst());
        if (fagsakinformasjon.isEmpty() || !fagsakinformasjon.get().aktørId().equals(aktøridSøker)) {
            throw new IllegalStateException("Saksnummer er ikke knyttet til søker");
        }

    }
}
