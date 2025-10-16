package no.nav.foreldrepenger.soknad.server;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ServiceStarterListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CDI.current().select(ApplicationServiceStarter.class).get().startServices();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        CDI.current().select(ApplicationServiceStarter.class).get().stopServices();
    }
}
