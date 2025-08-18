package no.nav.foreldrepenger.mottak.domene.vedlegg.sjekkere.virusscan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.mottak.domene.vedlegg.Vedlegg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class VirusScanTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(VirusScanTjeneste.class);

    private VirusScanKlient virusScanKlient;

    public VirusScanTjeneste() {
        // CDI
    }

    @Inject
    public VirusScanTjeneste(VirusScanKlient virusScanKlient) {
        this.virusScanKlient = virusScanKlient;
    }

    public void scan(Vedlegg vedlegg) {
        if (Environment.current().isLocal()) {
            LOG.info("Scanning er deaktivert i lokal utvikling");
            return; // Skipping scan in local development
        }

        virusScanKlient.scan(vedlegg.bytes(), vedlegg.uuid());
    }
}
