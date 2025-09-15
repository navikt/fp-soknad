package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;

public record VurderFagsystemResultat(SendTil destinasjon, String saksnummer) {

    static VurderFagsystemResultat fra(BehandlendeFagsystemDto data) {
        if (data == null) {
            return new VurderFagsystemResultat(SendTil.GOSYS, null);
        }
        var sendesTil = data.isBehandlesIVedtaksløsningen() || data.isSjekkMotInfotrygd()
            ? SendTil.FPSAK
            : SendTil.GOSYS;
        return new VurderFagsystemResultat(sendesTil, data.getSaksnummer().orElseThrow());
    }

    public enum SendTil {
        FPSAK,
        GOSYS
    }

}
