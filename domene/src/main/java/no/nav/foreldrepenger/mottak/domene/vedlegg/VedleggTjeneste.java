package no.nav.foreldrepenger.mottak.domene.vedlegg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VedleggTjeneste {


    public Optional<Vedlegg> hentVedlegg(@Valid UUID uuid) {
        return null; // TODO
    }

    public UUID lagreVedlegg(Vedlegg vedlegg) {
        return null; // TODO
    }
}
