package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;

class VurderFagsystemResultatTest {


    @Test
    void manuell_vurdering_skal_mappes_til_GOSYS() {
        var behandlendeFagsystemDto = new BehandlendeFagsystemDto();
        behandlendeFagsystemDto.setBehandlesIVedtaksløsningen(false);
        behandlendeFagsystemDto.setSjekkMotInfotrygd(false);
        behandlendeFagsystemDto.setManuellVurdering(true);

        var resultat = VurderFagsystemResultat.fra(behandlendeFagsystemDto);
        assertThat(resultat.destinasjon()).isEqualTo(VurderFagsystemResultat.SendTil.GOSYS);
        assertThat(resultat.saksnummer()).isNull();
    }

    @Test
    void behandles_i_vedtaksløsningen_skal_mappes_til_FPSAK_med_saksnummer() {
        var saksnummer = "123456789";
        var behandlendeFagsystemDto = new BehandlendeFagsystemDto(saksnummer);
        behandlendeFagsystemDto.setBehandlesIVedtaksløsningen(true);

        var resultat = VurderFagsystemResultat.fra(behandlendeFagsystemDto);
        assertThat(resultat.destinasjon()).isEqualTo(VurderFagsystemResultat.SendTil.FPSAK);
        assertThat(resultat.saksnummer()).isEqualTo(saksnummer);
    }
}
