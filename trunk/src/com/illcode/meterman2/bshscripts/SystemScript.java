package com.illcode.meterman2.bshscripts;

import com.illcode.meterman2.model.*;

import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.Meterman2.gm;

/**
 * Not actually used in the project, but just a faux-class to facilitate
 * IDE code-assistance for writing system-script.bsh.
 */
class SystemScript
{
    private StringBuilder outputBuilder;

    ///////////////////////////////////////////
    // Here is where the script methods begin//
    ///////////////////////////////////////////

    /**
     * Output a simple text string.
     * @param text text to output
     */
    void out(String text) {
        outputBuilder.append(text);
    }

    /**
     * Output the text of a passage found in our bundle group.
     * @param id passage ID to output
     */
    void outPassage(String id) {
        outputBuilder.append(bundles.getPassage(id).getText());
    }

    /**
     * Print text to the main text area.
     */
    void print(String text) {
        gm.print(text);
    }

    /**
     * Print text followed by a newline to the main text area.
     */
    void println(String text) {
        gm.println(text);
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the"), taking into
     * account proper names.
     * @param e entity
     * @param capitalize whether to capitalize the definite article
     */
    String defName(Entity e, boolean capitalize) {
        return GameUtils.defName(e, capitalize);
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the") in lowercase,
     * taking into account proper names.
     * @param e entity
     */
    String defName(Entity e) {
        return GameUtils.defName(e);
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other"), taking into account
     * proper names. If {@code e.getIndefiniteArticle()} returns null, we use "an" for names that begin with
     * a vowel, and "a" otherwise.
     * @param e entity
     * @param capitalize whether to capitalize the indefinite article
     */
    String indefName(Entity e, boolean capitalize) {
        return GameUtils.indefName(e, capitalize);
    }

    /** Return the name of the entity prefixed by the indefinite article ("a/an/other") in lowercase. */
    String indefName(Entity e) {
        return GameUtils.indefName(e);
    }
}
