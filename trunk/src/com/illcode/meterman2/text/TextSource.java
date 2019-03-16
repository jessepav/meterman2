package com.illcode.meterman2.text;

/**
 * Encapsulates a source of text, be it a simple String, or a script, or XML element, etc.
 */
public interface TextSource
{
    /** Return the text represented by this source. */
    String getText();

    /**
     * Get the text represented by this source, with variable bindings.
     * @param bindings an array of variable bindings. How these are interpreted depends on
     *                 the particular implementation.
     * @return the text represented by this source
     */
    String getTextWithBindings(String... bindings);

    /**
     * Get the text represented by this source, with arguments made available in an implementation-specific way.
     * @param args arguments
     * @return the text represented by this source
     */
    String getTextWithArgs(Object... args);
}
