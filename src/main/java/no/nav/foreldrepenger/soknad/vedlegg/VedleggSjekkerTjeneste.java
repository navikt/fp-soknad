package no.nav.foreldrepenger.soknad.vedlegg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.vedlegg.sjekkere.VedleggSjekker;

@ApplicationScoped
public class VedleggSjekkerTjeneste {

    private final Instance<VedleggSjekker> vedleggSjekkerInstance;

    @Inject
    public VedleggSjekkerTjeneste(@Any Instance<VedleggSjekker> vedleggSjekkerInstance) {
        this.vedleggSjekkerInstance = vedleggSjekkerInstance;
    }

    public void sjekkVedlegg(Vedlegg vedlegg) {
        vedleggSjekkerInstance.forEach(s -> s.sjekk(vedlegg));
    }
}
