package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.text.TextSource;

import java.util.ArrayList;
import java.util.List;

public class BaseEntityImpl implements EntityImpl
{
    protected TextSource description;
    protected List<Action> actionList;

    public BaseEntityImpl() {
        actionList = new ArrayList<>(8);
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
        actionList.clear();
        return actionList;
    }

    public boolean processAction(Entity e, Action action) {
        return false;
    }

    public boolean objectAction(Entity e, Action action, Entity selectedEntity) {
        return false;
    }

    public Object getState(Entity e) {
        return null;
    }

    public void restoreState(Entity e, Object state) {
        // empty
    }

    public void gameStarting(Entity e) {
        // empty
    }
}
