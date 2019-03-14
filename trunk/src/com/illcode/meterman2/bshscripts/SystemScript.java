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
}
