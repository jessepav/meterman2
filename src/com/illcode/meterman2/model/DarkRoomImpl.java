package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemMessages;
import com.illcode.meterman2.event.TurnListener;
import com.illcode.meterman2.text.TextSource;

/**
 * Implementation class for dark rooms. If the room is dark, we return a different "dark descrption".
 */
public class DarkRoomImpl extends BaseRoomImpl
{
    protected TextSource darkDescription;

    @Override
    public String getDescription(Room r) {
        if (DarkRoom.isDark(r))
            return getDarkDescription();
        else
            return super.getDescription(r);
    }

    public String getDarkDescription() {
        return darkDescription != null ? darkDescription.getText() :
            Meterman2.bundles.getPassage(SystemMessages.DARKROOM_DESCRIPTION).getText();
    }

    public void setDarkDescription(TextSource darkDescription) {
        this.darkDescription = darkDescription;
    }

}
