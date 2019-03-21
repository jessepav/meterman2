package com.illcode.meterman2.text;

import com.illcode.meterman2.bundle.XBundle;

/**
 * A {@code TextSource} backed by a String.
 */
public final class StringSource implements TextSource
{
    private String str;

    public StringSource(String str) {
        this.str = str;
    }

    public String getText() {
        return str;
    }

    /**
     * @param bindings ignored
     */
    public String getTextWithBindings(String... bindings) {
        return getText();
    }

    /**
     * @param args ignored
     */
    public String getTextWithArgs(Object... args) {
        return getText();
    }

    public String toString() {
        return getText();
    }
}
