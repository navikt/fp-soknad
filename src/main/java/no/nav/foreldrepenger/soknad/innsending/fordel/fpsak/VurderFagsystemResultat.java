package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;

public record VurderFagsystemResultat(SendTil destinasjon, String saksnummer) {

    static VurderFagsystemResultat fra(BehandlendeFagsystemDto data) {
        if (data == null || !data.isBehandlesIVedtaksløsningen()) {
            return new VurderFagsystemResultat(SendTil.GOSYS, null);
        }
        return new VurderFagsystemResultat(SendTil.FPSAK, data.getSaksnummer().orElseThrow());
    }

    public enum SendTil {
        FPSAK,
        GOSYS
    }
}
