package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.SystemActions;

import java.util.ArrayList;
import java.util.List;

import static com.illcode.meterman2.SystemAttributes.*;

/**
 * Implementation for container entities.
 */
public class ContainerImpl extends BaseEntityImpl
{
    private List<MMActions.Action> actions;
    private MMActions.Action putInAction, takeOutAction;

    public ContainerImpl() {
        super();
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        if (!(e instanceof Container))
            return super.getActions(e);
        Container c = (Container) e;
        if (actions == null) {
            actions = new ArrayList<>(6);
            putInAction = SystemActions.CONTAINER_PUT.formattedTextCopy(c.getInPrep());
            takeOutAction = SystemActions.CONTAINER_TAKE.formattedTextCopy(c.getOutPrep());
        }
        actions.clear();
        // TODO: ContainerImpl
        if (c.getAttributes().get(LOCKED)) {
            actions.add(SystemActions.UNLOCK);
        }

       return actions;
    }

    @Override
    public boolean processAction(Entity e, MMActions.Action action) {

        return false;
    }
}
