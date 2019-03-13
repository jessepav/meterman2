package com.illcode.meterman2.text;

/**
 * A {@code TextSource} backed by a String.
 */
public class StringSource implements TextSource
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

    public String toString() {
        return getText();
    }
}
