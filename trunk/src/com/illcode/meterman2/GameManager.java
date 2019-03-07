package com.illcode.meterman2;

import com.illcode.meterman2.ui.MMUI;

import static com.illcode.meterman2.Meterman2.ui;

public class GameManager
{
    void dispose() {

    }

    // Games should call these methods instead of going directly to Meterman2.ui
    // so that we can process the text, if the situation warrants.
    /**
     * Appends text to the main text area.
     * @param text text to append
     */
    public void appendText(String text) {ui.appendText(text);}

    /**
     * Appends a newline to the main text area.
     */
    public void appendNewline() {ui.appendNewline();}

    /**
     * Appends text to the main text area, followed by a newline.
     * @param text text to append
     */
    public void appendTextLn(String text) {ui.appendTextLn(text);}
}
