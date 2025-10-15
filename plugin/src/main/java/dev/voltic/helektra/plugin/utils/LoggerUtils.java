package dev.voltic.helektra.plugin.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.Level;

public final class LoggerUtils {

    private LoggerUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void blockMongoDBLogs() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        String[] mongoLoggers = {
            "org.mongodb",
            "org.mongodb.driver",
            "org.mongodb.driver.client",
            "org.mongodb.driver.cluster",
            "org.mongodb.driver.connection",
            "org.mongodb.driver.protocol",
            "org.mongodb.driver.management",
            "org.mongodb.driver.protocol.command",
            "org.mongodb.driver.authenticator",
            "com.mongodb"
        };

        for (String name : mongoLoggers) {
            LoggerConfig loggerConfig = config.getLoggerConfig(name);
            if (!loggerConfig.getName().equals(name)) {
                LoggerConfig newConfig = new LoggerConfig(name, Level.OFF, false);
                config.addLogger(name, newConfig);
            } else {
                loggerConfig.setLevel(Level.OFF);
            }
        }

        context.updateLoggers();
    }
}
