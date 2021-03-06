package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity that the user can interact with, like a person to talk to, or book to read,
 * or a computer system to look stuff up in.
 */
public class InteractingEntityImpl extends BaseEntityImpl
{
    private InteractSupport interactSupport;

    public InteractingEntityImpl(Entity e) {
        super();
        interactSupport = new InteractSupport(e);
    }

    public InteractSupport getInteractSupport() {
        return interactSupport;
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        List<MMActions.Action> actions = super.getActions(e);
        actions.add(interactSupport.getInteractAction());
        return actions;
    }

    @Override
    public boolean processAction(Entity e, MMActions.Action action) {
        if (action.equals(interactSupport.getInteractAction())) {
            interactSupport.interact();
            return true;
        } else {
            return super.processAction(e, action);
        }
    }

    @Override
    public Object getState(Entity e) {
        final Object[] stateObjs = new Object[2];
        stateObjs[0] = interactSupport.getState();
        stateObjs[1] = super.getState(e);
        return stateObjs;
    }

    @Override
    public void restoreState(Entity e, Object state) {
        final Object[] stateObjs = (Object[]) state;
        interactSupport.restoreState(stateObjs[0]);
        super.restoreState(e, stateObjs[1]);
    }

}
