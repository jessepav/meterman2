package com.illcode.meterman2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

public final class MMLogging
{
    public static Logger logger;

    /**
     * Initializes our static logger, pulling the logs directory from the "log-path" pref.
     * Henceforth, {@link #logger} will be ready for action.
     * @throws IOException
     */
    public static void initializeLogging() throws IOException {
        System.setProperty("java.util.logging.config.file", "config/logging.properties");
        LogManager.getLogManager().readConfiguration();

        Path logPath = Paths.get(Utils.pref("log-path", "logs"));
        if (Files.notExists(logPath))
            Files.createDirectories(logPath);
        logger = Logger.getLogger("com.illcode.meterman2");
        logger.addHandler(new FileHandler(logPath.toString() + "/meterman2-%g.log", 50000, 10, true));

        logger.info("Meterman2 Logger Initialized. Default Charset: " + Charset.defaultCharset());
    }

    /**
     * In case someone wants to use use {@link #logger} to print to the console without the full rotating
     * log machinery that we normally set up.
     * @param name name of the logger
     */
    public static void initSimpleLogging(String name) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: [%1$tF %1$tT] %5$s %6$s%n");
        LogManager.getLogManager().readConfiguration();

        logger = Logger.getLogger(name);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }
}
