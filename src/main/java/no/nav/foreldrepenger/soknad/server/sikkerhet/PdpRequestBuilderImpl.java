package no.nav.foreldrepenger.soknad.server.sikkerhet;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        Set<String> saksnumre = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        var builder = AppRessursData.builder()
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.FNR));
        saksnumre.stream().findFirst().ifPresent(builder::medSaksnummer);
        return builder.build();
    }
}
