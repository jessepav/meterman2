package com.illcode.meterman2;

import com.illcode.meterman2.model.Entity;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Utility methods that apply to the game world or interface.
 */
public final class GameUtils
{
    /**
     * Return the name of the entity prefixed by the definite article ("the"), taking into
     * account proper names.
     * @param e entity
     * @param capitalize whether to capitalize the definite article
     */
    public static String defName(Entity e, boolean capitalize) {
        String name = e.getName();
        if (name.isEmpty())
            return "";
        if (e.getAttributes().get(SystemAttributes.PROPER_NAME))
            return name;
        String defArt = capitalize ? "The " : "the ";
        return defArt + name;
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the") in lowercase,
     * taking into account proper names.
     * @param e entity
     */
    public static String defName(Entity e) {
        return defName(e, false);
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other"), taking into account
     * proper names. If {@code e.getIndefiniteArticle()} returns null, we use "an" for names that begin with
     * a vowel, and "a" otherwise.
     * @param e entity
     * @param capitalize whether to capitalize the indefinite article
     */
    public static String indefName(Entity e, boolean capitalize) {
        String name = e.getName();
        if (name.isEmpty())
            return "";
        if (e.getAttributes().get(SystemAttributes.PROPER_NAME))
            return name;
        String indefArt = e.getIndefiniteArticle();
        if (indefArt == null) {
            char c = Character.toLowerCase(name.charAt(0));
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u')
                indefArt = "an";
            else
                indefArt = "a";
        }
        if (capitalize)
            indefArt = WordUtils.capitalize(indefArt);
        return indefArt + " " + name;
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other") in lowercase.
     * @param e entity
     */
    public static String indefName(Entity e) {
        return indefName(e, false);
    }
}
