package com.illcode.meterman2.text;

import com.illcode.meterman2.bundle.XBundle;

/**
 * A {@code TextSource} backed by a String.
 */
public final class StringSource implements TextSource
{
    private final String str;
    private final XBundle bundle;

    public StringSource(String str) {
        this.str = str;
        this.bundle = null;
    }

    public StringSource(String str, XBundle bundle) {
        this.str = str;
        this.bundle = bundle;
    }

    public String getText() {
        if (bundle != null)
            // the text was already normalized when it was passed into the constructor
            // in XBundle.elementTextSource().
            return bundle.unescapeText(str);
        else
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
