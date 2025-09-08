package no.nav.foreldrepenger.soknad.vedlegg;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;

@ApplicationScoped
public class VedleggTjeneste {


    public Optional<Vedlegg> hentVedlegg(@Valid UUID uuid) {
        return null; // TODO
    }

    public UUID lagreVedlegg(Vedlegg vedlegg) {
        return null; // TODO
    }
}
