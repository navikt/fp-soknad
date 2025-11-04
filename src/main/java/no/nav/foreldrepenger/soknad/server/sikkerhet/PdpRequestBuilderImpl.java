package no.nav.foreldrepenger.soknad.server.sikkerhet;

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
        var builder = AppRessursData.builder().leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.FNR));
        return builder.build();
    }
}
