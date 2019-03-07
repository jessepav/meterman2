package com.illcode.meterman2.model;

import com.illcode.meterman2.text.TextSource;
import com.illcode.meterman2.ui.UIConstants;

public class BaseRoomImpl implements RoomImpl
{
    protected String name;
    protected String exitName;
    protected TextSource description;
    protected Room[] exits;
    protected String[] exitLabels;

    protected BaseRoomImpl() {
        exits = new Room[UIConstants.NUM_EXIT_BUTTONS];
        exitLabels = new String[UIConstants.NUM_EXIT_BUTTONS];
    }

    public String getName(Room r) {
        return name != null ? name : "[name]";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExitName(Room r) {
        return exitName != null ? exitName : getName(r);
    }

    public void setExitName(String exitName) {
        this.exitName = exitName;
    }

    public String getDescription(Room r) {
        return description != null ? description.getText() : "[description]";
    }

    public void setDescription(TextSource description) {
        this.description = description;
    }

    public Room getExit(Room r, int direction) {
        return exits[direction];
    }

    public void setExit(int direction, Room destination) {
        exits[direction] = destination;
    }

    public String getExitLabel(Room r, int direction) {
        if (exitLabels[direction] != null)
            return exitLabels[direction];
        else if (exits[direction] != null)
            return exits[direction].getExitName();
        else
            return null;
    }

    public void setExitLabel(int direction, String label) {
        exitLabels[direction] = label;
    }

    public void entered(Room r, Room fromRoom) {
        // empty
    }

    public boolean exiting(Room r, Room toRoom) {
        return false;
    }
}
