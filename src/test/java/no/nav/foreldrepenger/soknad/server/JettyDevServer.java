package no.nav.foreldrepenger.soknad.server;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.server.localdev.LocalDevProperties;

public class JettyDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();

    static void main(String[] args) throws Exception {
        // Konfigurerer tasker til å polle mer aggressivt, gjør at verdikjede kjører raskere lokalt
        System.setProperty("task.manager.polling.delay", "40");
        System.setProperty("task.manager.runner.threads", "4");
        LocalDevProperties.setPropertiesForLocalDev();
        jettyServer(args).bootStrap();
    }

    protected static JettyDevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyDevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyDevServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    private JettyDevServer(int serverPort) {
        super(serverPort);
    }
}
