package com.illcode.meterman2.text;

import org.apache.commons.lang3.StringUtils;

/**
 * Static methods that pertain to our inline markup.
 */
public final class Markup
{
    public static final String EMPH_TAG = "[em]";
    public static final String EMPH_CLOSE_TAG = "[/em]";

    private static final String[] TAGS = {EMPH_TAG, EMPH_CLOSE_TAG};
    private static final String[] TAG_REPLACEMENTS = {"*", "*"};

    /**
     * Returns a plain text approximation of text with embedded markup.
     * @param text text with markup
     */
    public static String plainTextMarkup(String text) {
        return StringUtils.replaceEach(text, TAGS, TAG_REPLACEMENTS);
    }
}
