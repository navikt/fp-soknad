package no.nav.foreldrepenger.soknad.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.ee11.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee11.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee11.servlet.DefaultServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.ee11.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;

public class JettyServer {
    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private static final Environment ENV = Environment.current();
    private static final String APPLICATION = "jakarta.ws.rs.Application";

    private static final String CONTEXT_PATH = "/fpsoknad";

    private final Integer serverPort;

    JettyServer(int serverPort) {
        this.serverPort = serverPort;
    }

    static void main() throws Exception {
        jettyServer().bootStrap();
    }

    private static JettyServer jettyServer() {
        return new JettyServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    private static ContextHandler createContext() {
        var ctx = new ServletContextHandler(CONTEXT_PATH, ServletContextHandler.NO_SESSIONS);

        // Sikkerhet
        ctx.setSecurityHandler(simpleConstraints());

        // Servlets
        registerDefaultServlet(ctx);
        registerServlet(ctx, 0, InternalApiConfig.API_URI, InternalApiConfig.class);
        registerServlet(ctx, 1, ApiConfig.API_URI, ApiConfig.class);

        // Starter tjenester
        ctx.addEventListener(new ServiceStarterListener());

        // Enable Weld + CDI
        ctx.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        ctx.addServletContainerInitializer(new CdiServletContainerInitializer());
        ctx.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());
        return ctx;
    }

    private static void registerDefaultServlet(ServletContextHandler context) {
        var defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, "/*");
    }

    private static void registerServlet(ServletContextHandler context, int prioritet, String path, Class<?> appClass) {
        var servlet = new ServletHolder(new ServletContainer());
        servlet.setInitOrder(prioritet);
        servlet.setInitParameter(APPLICATION, appClass.getName());
        context.addServlet(servlet, path + "/*");
    }

    void bootStrap() throws Exception {
        System.setProperty("task.manager.runner.threads", "4");
        var dataSource = setupDataSource();
        migrer(dataSource);
        start();
    }

    private static void migrer(DataSource dataSource) {
        var flyway = flywayConfig(dataSource);
        flyway.load().migrate();
    }

    public static FluentConfiguration flywayConfig(DataSource dataSource) {
        return Flyway.configure().dataSource(dataSource).locations("classpath:/db/postgres/defaultDS").baselineOnMigrate(true);
    }

    public static DataSource setupDataSource() throws NamingException {
        var dataSource = dataSource();
        new EnvEntry("jdbc/defaultDS", dataSource);
        return dataSource;
    }

    public static DataSource dataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl(ENV.getProperty("NAIS_DATABASE_FPSOKNAD_FPSOKNAD_JDBC_URL"));
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(2));
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(12);
        config.setInitializationFailTimeout(30000);
        config.setConnectionTestQuery("select 1");
        config.setDriverClassName("org.postgresql.Driver");
        config.setAutoCommit(false);

        // optimaliserer inserts for postgres
        var dsProperties = new Properties();
        dsProperties.setProperty("reWriteBatchedInserts", "true");
        dsProperties.setProperty("logServerErrorDetail", "false"); // skrur av batch exceptions som lekker statements i åpen logg
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    private void start() throws Exception {
        LOG.info("Starter server");
        var server = new Server(getServerPort());
        server.setConnectors(createConnectors(server).toArray(new Connector[]{}));
        server.setHandler(createContext());
        server.setStopAtShutdown(true);
        server.setStopTimeout(10_000);
        server.start();
        LOG.info("Server startet på port: {}", getServerPort());
        server.join();
    }

    private List<Connector> createConnectors(Server server) {
        List<Connector> connectors = new ArrayList<>();
        var httpConnector = new ServerConnector(server);
        httpConnector.setPort(getServerPort());
        connectors.add(httpConnector);
        return connectors;
    }

    private static ConstraintSecurityHandler simpleConstraints() {
        var handler = new ConstraintSecurityHandler();
        // Slipp gjennom kall fra plattform til JaxRs. Foreløpig kun behov for GET
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, InternalApiConfig.API_URI + "/*"));
        // Slipp gjennom til autentisering i JaxRs / auth-filter
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, ApiConfig.API_URI + "/*"));
        // Alt annet av paths og metoder forbudt - 403
        handler.addConstraintMapping(pathConstraint(Constraint.FORBIDDEN, "/*"));
        return handler;
    }

    private static ConstraintMapping pathConstraint(Constraint constraint, String path) {
        var mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(path);
        return mapping;
    }

    private Integer getServerPort() {
        return this.serverPort;
    }

}
