package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMAttributes;

import java.util.List;
import java.util.Map;

public class BaseEntityImpl implements EntityImpl
{
    protected BaseEntityImpl() {
    }

    public String getName(Entity e) {
        return null;
    }

    public String getDescription(Entity e) {
        return null;
    }

    public void lookInRoom(Entity e) {

    }

    public void enterScope(Entity e) {

    }

    public void exitingScope(Entity e) {

    }

    public void taken(Entity e) {

    }

    public void dropped(Entity e) {

    }

    public Room getRoom(Entity e) {
        return null;
    }

    public void setRoom(Entity e, Room room) {

    }

    public List<String> getActions(Entity e) {
        return null;
    }

    public boolean processAction(Entity e, MMActions.Action action) {
        return false;
    }
}
