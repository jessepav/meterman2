package com.illcode.meterman2;

/**
 * Classes that provide text to be shown in the status bar.
 */
public interface StatusBarProvider
{
    /**
     * Return the text to be shown in a given status bar position.
     * @param labelPos one of the <tt>LABEL</tt> positions in {@code UIConstants}
     * @return text to be shown, or null for no text
     */
    String getStatusText(int labelPos);
}
