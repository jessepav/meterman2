package com.illcode.meterman2.text;

import com.illcode.meterman2.bundle.XBundle;

import java.util.Formatter;
import java.util.IllegalFormatException;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * A text source whose output is obtained by rendering a printf-style format string. It does not have access
 * to the script or template namespaces, and thus is used mostly for simple system messages.
 * <p/>
 * This class uses a single instance of a Formatter, and thus is not thread-safe. You must synchronize on
 * the class object (or equivalent class-wide lock) if accessed from multiple threads.
 */
public final class FormatStringSource implements TextSource
{
    private static Formatter formatter;
    private static StringBuilder strBuilder;

    private final String format;
    private final XBundle bundle;

    /**
     * Create a new format-string source.
     * @param format a printf-style format string
     * @param bundle XBundle used to format the template output
     * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax">Format String Syntax</a>
     */
    public FormatStringSource(String format, XBundle bundle) {
        this.format = format;
        this.bundle = bundle;
    }

    public void dispose() {
        // empty
    }

    /**
     * With no format arguments, just return the format string itself.
     */
    public String getText() {
        return format;
    }

    /**
     * Bindings are not supported by format-strings, so just return the format string itself.
     */
    public String getTextWithBindings(String... bindings) {
        return format;
    }

    /**
     * @param args used as arguments to the format string.
     */
    public String getTextWithArgs(Object... args) {
        if (args == null)
            return format;

        if (formatter == null) {
            strBuilder = new StringBuilder(128);
            formatter = new Formatter(strBuilder);
        }
        try {
            formatter.format(format, args);
            return bundle.formatText(strBuilder.toString());
        } catch (IllegalFormatException ex) {
            logger.warning("Illegal format in FormatStringSource: " + ex.getMessage());
            return "[error]";
        } finally {
            strBuilder.setLength(0);
        }
    }

    public String toString() {
        return getText();
    }
}

