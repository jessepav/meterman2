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
    public String getText(String... bindings) {
        return getText();
    }

    public String toString() {
        return getText();
    }
}
