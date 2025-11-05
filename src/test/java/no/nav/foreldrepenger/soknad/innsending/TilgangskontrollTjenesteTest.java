package no.nav.foreldrepenger.soknad.innsending;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.kontrakter.felles.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.PdlKlientSystem;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;
import no.nav.vedtak.exception.ManglerTilgangException;

@ExtendWith(MockitoExtension.class)
class TilgangskontrollTjenesteTest {

    @Mock
    private InnloggetBruker innloggetBruker;
    @Mock
    private FpsakTjeneste fpsakTjeneste;
    @Mock
    private PdlKlientSystem pdlKlientSystem;
    private TilgangskontrollTjeneste tilgangskontrollTjeneste;

    @BeforeEach
    void setUp() {
        tilgangskontrollTjeneste = new TilgangskontrollTjeneste(innloggetBruker, fpsakTjeneste, pdlKlientSystem);
    }

    @Test
    void søker_fra_søknad_er_forskjellig_fra_innlogget_bruker_skal_hive_exception() {
        var dummyFnr = new Fødselsnummer("1234567890");
        when(innloggetBruker.brukerFraKontekst()).thenReturn(dummyFnr.value());

        assertDoesNotThrow(() -> tilgangskontrollTjeneste.validerSøkerFraKontekstErSammeSomSøknad(dummyFnr));
    }

    @Test
    void søker_fra_søknad_er_lik_fra_innlogget_bruker_skal_ikke_hive_exception() {
        var dummyFnr1 = new Fødselsnummer("1234567890");
        var dummyFnr2 = new Fødselsnummer("9876543210");
        when(innloggetBruker.brukerFraKontekst()).thenReturn(dummyFnr2.value());

        var ex = assertThrows(ManglerTilgangException.class, () -> tilgangskontrollTjeneste.validerSøkerFraKontekstErSammeSomSøknad(dummyFnr1));
        assertThat(ex.getMessage()).contains("Søker som er angitt i innsendt søknad er ulik fra innlogget bruker");
    }

    @Test
    void søker_er_knyttet_til_angitt_saksnummer_happycase() {
        var akørId = "9912345678910";
        var dummyFnr = new Fødselsnummer("1234567890");
        when(innloggetBruker.brukerFraKontekst()).thenReturn(dummyFnr.value());
        when(fpsakTjeneste.finnFagsakInfomasjon(any())).thenReturn(Optional.of(new FagsakInfomasjonDto(akørId, null)));
        when(pdlKlientSystem.aktørId(dummyFnr.value())).thenReturn(new AktørId(akørId));

        assertDoesNotThrow(() -> tilgangskontrollTjeneste.validerSaksnummerKnyttetTilSøker(new Saksnummer("1234567890")));
    }

    @Test
    void søker_er_ikke_knyttet_til_sak_hiver_exception() {
        var innloggetSøkersAktørid = "9912345678910";
        var aktørIdRegistrertPåSak = "999999999";
        var dummyFnr = new Fødselsnummer("1234567890");
        var saksnummer = new Saksnummer("1234567890");
        when(innloggetBruker.brukerFraKontekst()).thenReturn(dummyFnr.value());
        when(fpsakTjeneste.finnFagsakInfomasjon(any())).thenReturn(Optional.of(new FagsakInfomasjonDto(aktørIdRegistrertPåSak, null)));
        when(pdlKlientSystem.aktørId(dummyFnr.value())).thenReturn(new AktørId(innloggetSøkersAktørid));

        var ex = assertThrows(ManglerTilgangException.class, () -> tilgangskontrollTjeneste.validerSaksnummerKnyttetTilSøker(saksnummer));
        assertThat(ex.getMessage()).contains("Saksnummer spesifisert i innsending er ikke knyttet til søker.");
        assertThat(innloggetSøkersAktørid).isNotEqualTo(aktørIdRegistrertPåSak);
    }


}
