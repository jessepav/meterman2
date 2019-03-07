package com.illcode.meterman2;

import com.illcode.meterman2.ui.MMUI;

import static com.illcode.meterman2.Meterman2.ui;

public final class GameManager
{
    void dispose() {

    }

    /**
     * Print text to the main UI text area. Games should call this method instead of going directly
     * to Meterman2.ui so that we can process or buffer the text, if the situation warrants.
     * @param text text to print
     */
    public void print(String text) {
        ui.appendText(text);
    }

    /**
     * Print text to the main UI text area, followed by a newline.
     * @param text text to print
     */
    public void println(String text) {
        print(text + "\n");
    }
}
