package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BaseEntityImpl implements EntityImpl
{
    protected String name;
    protected String description;

    protected BaseEntityImpl() {
        name = "[name]";
        description = "[description]";
    }

    public String getName(Entity e) {
        return name;
    }

    public String getDescription(Entity e) {
        return description;
    }

    public void lookInRoom(Entity e) {
        // empty
    }

    public void enterScope(Entity e) {
        // empty
    }

    public void exitingScope(Entity e) {
        // empty
    }

    public void taken(Entity e) {
        // empty
    }

    public void dropped(Entity e) {
        // empty
    }

    public List<String> getActions(Entity e) {
        return Collections.emptyList();
    }

    public boolean processAction(Entity e, MMActions.Action action) {
        return false;
    }
}
