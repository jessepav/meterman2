package com.illcode.meterman2.text;

/**
 * A {@code TextSource} backed by a String.
 */
public final class StringSource implements TextSource
{
    public String str;

    public StringSource() {
        str = "";
    }

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
        return null;
    }

    public String toString() {
        return getText();
    }
}
