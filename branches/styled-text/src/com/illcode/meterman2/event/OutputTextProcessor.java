package com.illcode.meterman2.event;

import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;

/**
 * An OutputTextProcessor is notified when the text sent to the main text area
 * has been gathered and is ready for displayed.
 */
public interface OutputTextProcessor extends GameEventHandler
{
    /**
     * Called when the text that will be shown has been gathered and is about to be displayed. The listener
     * can modify the text shown by modifying the contents of the StringBuilder.
     * @param sb StringBuilder holding the text to be shown
     */
    void outputTextReady(StringBuilder sb);
}
