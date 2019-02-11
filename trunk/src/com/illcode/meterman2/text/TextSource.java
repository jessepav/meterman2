package com.illcode.meterman2.text;

/**
 * Encapsulates a source of text, be it a simple String, or a script, or XML element, etc.
 */
public interface TextSource
{
    /** Return the text represented by this TextSource */
    String getText();
}
