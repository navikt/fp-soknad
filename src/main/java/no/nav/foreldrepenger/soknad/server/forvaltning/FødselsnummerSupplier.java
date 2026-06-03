package no.nav.foreldrepenger.soknad.server.forvaltning;

import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

import java.util.function.Function;

public class FødselsnummerSupplier implements Function<Object, AbacDataAttributter> {
    @Override
    public AbacDataAttributter apply(Object obj) {
        var req = (Fødselsnummer) obj;
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.FNR, req.value());
    }
}
