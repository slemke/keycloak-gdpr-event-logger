package io.github.slemke.gdpr;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class GDPREventListenerFactory implements EventListenerProviderFactory {

    public static final String ID = "gdpr-jboss-logging";
    
    private static final Logger logger = Logger.getLogger("org.keycloak.events");

    private Logger.Level successLevel;
    private Logger.Level errorLevel;
    private boolean sanitize;
    private Character quotes;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new GDPREventListener(session, logger, successLevel, errorLevel, quotes, sanitize);
    }

    @Override
    public void init(Scope config) {
        successLevel = Logger.Level.valueOf(config.get("success-level", "debug").toUpperCase());
        errorLevel = Logger.Level.valueOf(config.get("error-level", "warn").toUpperCase());
        sanitize = config.getBoolean("sanitize", true);
        String quotesString = config.get("quotes", "\"");
        if (!quotesString.equals("none") && quotesString.length() > 1) {
            logger.warn("Invalid quotes configuration, it should be none or one character to use as quotes. Using default \" quotes");
            quotesString = "\"";
        }
        quotes = quotesString.equals("none")? null : quotesString.charAt(0);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) { }

    @Override
    public void close() { }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        String[] logLevels = Arrays.stream(Logger.Level.values())
                .map(Logger.Level::name)
                .map(String::toLowerCase)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        return ProviderConfigurationBuilder.create()
                .property()
                .name("success-level")
                .type("string")
                .helpText("The log level for success messages.")
                .options(logLevels)
                .defaultValue("debug")
                .add()
                .property()
                .name("error-level")
                .type("string")
                .helpText("The log level for error messages.")
                .options(logLevels)
                .defaultValue("warn")
                .add()
                .property()
                .name("sanitize")
                .type("boolean")
                .helpText("If true the log messages are sanitized to avoid line breaks. If false messages are not sanitized.")
                .defaultValue("true")
                .add()
                .property()
                .name("quotes")
                .type("string")
                .helpText("The quotes to use for values, it should be one character like \" or '. Use \"none\" if quotes are not needed.")
                .defaultValue("\"")
                .add()
                .build();
    }
}
