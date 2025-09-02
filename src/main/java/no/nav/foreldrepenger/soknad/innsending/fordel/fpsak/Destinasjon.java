package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;

public record Destinasjon(ForsendelseStatus system, String saksnummer) {

    public static Destinasjon GOSYS = new Destinasjon(ForsendelseStatus.GOSYS, null);

    public boolean erGosys() {
        return ForsendelseStatus.GOSYS.equals(system);
    }
}
