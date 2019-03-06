package com.illcode.meterman2.model;

public class BaseRoomImpl implements RoomImpl
{
    protected BaseRoomImpl() {
    }

    public String getName(Room r) {
        return null;
    }

    public String getExitName(Room r) {
        return null;
    }

    public String getDescription(Room r) {
        return null;
    }

    public Room getExit(Room r, int direction) {
        return null;
    }

    public String getExitLabel(Room r, int direction) {
        return null;
    }

    public void entered(Room r, Room fromRoom) {

    }

    public boolean exiting(Room r, Room toRoom) {
        return false;
    }
}
