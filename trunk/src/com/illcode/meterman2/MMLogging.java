package com.illcode.meterman2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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

}
