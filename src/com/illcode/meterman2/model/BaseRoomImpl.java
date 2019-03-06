package com.illcode.meterman2.model;

import com.illcode.meterman2.ui.UIConstants;

public class BaseRoomImpl implements RoomImpl
{
    protected String name;
    protected String exitName;
    protected String description;
    protected Room[] exits;
    protected String[] exitLabels;

    protected BaseRoomImpl() {
        name = "[name]";
        exitName = null;
        description = "[description]";
        exits = new Room[UIConstants.NUM_EXIT_BUTTONS];
        exitLabels = new String[UIConstants.NUM_EXIT_BUTTONS];
    }

    public String getName(Room r) {
        return name;
    }

    public String getExitName(Room r) {
        return exitName != null ? exitName : name;
    }

    public String getDescription(Room r) {
        return description;
    }

    public Room getExit(Room r, int direction) {
        return exits[direction];
    }

    public String getExitLabel(Room r, int direction) {
        if (exitLabels[direction] != null)
            return exitLabels[direction];
        else if (exits[direction] != null)
            return exits[direction].getExitName();
        else
            return null;
    }

    public void entered(Room r, Room fromRoom) {
        // empty
    }

    public boolean exiting(Room r, Room toRoom) {
        return false;
    }
}
