package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.text.TextSource;

import java.util.Collections;
import java.util.List;

public class BaseEntityImpl implements EntityImpl
{
    protected String name;
    protected TextSource description;

    protected BaseEntityImpl() {
    }

    public String getName(Entity e) {
        return name != null ? name : "[name]";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription(Entity e) {
        return description != null ? description.getText() : "[description]";
    }

    public void setDescription(TextSource description) {
        this.description = description;
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

    public List<Action> getActions(Entity e) {
        return Collections.emptyList();
    }

    public boolean processAction(Entity e, Action action) {
        return false;
    }
}
