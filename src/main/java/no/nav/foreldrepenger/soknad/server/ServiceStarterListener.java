package no.nav.foreldrepenger.soknad.server;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ServiceStarterListener implements ServletContextListener {

    private ApplicationServiceStarter applicationServiceStarter;

    ServiceStarterListener() {
        // CDI
    }

    @Inject
    public ServiceStarterListener(ApplicationServiceStarter applicationServiceStarter) {
        this.applicationServiceStarter = applicationServiceStarter;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        applicationServiceStarter.startServices();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        applicationServiceStarter.stopServices();
    }
}
