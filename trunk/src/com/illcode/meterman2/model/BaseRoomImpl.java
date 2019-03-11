package com.illcode.meterman2.model;

import com.illcode.meterman2.text.TextSource;
import com.illcode.meterman2.ui.UIConstants;

public class BaseRoomImpl implements RoomImpl
{
    protected TextSource description;

    protected BaseRoomImpl() {
    }

    public String getDescription(Room r) {
        return description != null ? description.getText() : "[description]";
    }

    public void setDescription(TextSource description) {
        this.description = description;
    }

    public void entered(Room r, Room fromRoom) {
        // empty
    }

    public boolean exiting(Room r, Room toRoom) {
        return false;
    }
}
