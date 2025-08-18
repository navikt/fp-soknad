package no.nav.foreldrepenger.mottak.domene.vedlegg.sjekkere.virusscan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.mottak.domene.vedlegg.Vedlegg;
import no.nav.foreldrepenger.mottak.domene.vedlegg.sjekkere.VedleggSjekker;

@ApplicationScoped
public class ClamAvVirusScanner implements VedleggSjekker {

    private VirusScanKlient connection;

    public ClamAvVirusScanner() {
        // CDI
    }

    @Inject
    public ClamAvVirusScanner(VirusScanKlient connection) {
        this.connection = connection;
    }

    @Override
    public void sjekk(Vedlegg vedlegg) {
        connection.scan(vedlegg.bytes(), vedlegg.uuid());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [connection=" + connection + "]";
    }

}
