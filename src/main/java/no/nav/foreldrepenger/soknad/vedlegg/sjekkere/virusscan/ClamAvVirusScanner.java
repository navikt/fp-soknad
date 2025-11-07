package no.nav.foreldrepenger.soknad.vedlegg.sjekkere.virusscan;

import static no.nav.foreldrepenger.soknad.vedlegg.VedleggUtil.megabytes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.sjekkere.VedleggSjekker;

@ApplicationScoped
public class ClamAvVirusScanner implements VedleggSjekker {
    private static final Logger LOG = LoggerFactory.getLogger(ClamAvVirusScanner.class);
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
        var start = System.currentTimeMillis();
        connection.scan(vedlegg);
        var slutt = System.currentTimeMillis();
        LOG.info("Scan av virus tok {}ms for vedlegg av størrelsen {}MB", slutt - start, megabytes(vedlegg.bytes().length));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [connection=" + connection + "]";
    }

}
