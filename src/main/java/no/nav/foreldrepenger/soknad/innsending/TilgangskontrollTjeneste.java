package no.nav.foreldrepenger.soknad.innsending;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.PdlKlientSystem;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Fødselsnummer;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.Saksnummer;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;
import no.nav.vedtak.exception.ManglerTilgangException;

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
            throw new ManglerTilgangException("SOKNAD-1001", "Søker som er angitt i innsendt søknad er ulik fra innlogget bruker. Vennligst logg inn på nytt og prøv igjen.");
        }
    }

    protected void validerSaksnummerKnyttetTilSøker(Saksnummer saksnummer) {
        var fagsakinformasjon = fpsakTjeneste.finnFagsakInfomasjon(new SaksnummerDto(saksnummer.value()));
        var aktøridSøker = pdlKlientSystem.aktørId(innloggetBruker.brukerFraKontekst());
        if (fagsakinformasjon.isEmpty() || !fagsakinformasjon.get().aktørId().equals(aktøridSøker.value())) {
            throw new ManglerTilgangException("SOKNAD-1002", "Saksnummer spesifisert i innsending er ikke knyttet til søker.");
        }

    }
}
