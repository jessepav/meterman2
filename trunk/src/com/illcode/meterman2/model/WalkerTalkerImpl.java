package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;

import java.util.List;

/**
 * An entity implementation that combines {@link EachTurnEntityImpl} and {@link InteractingEntityImpl}
 */
public final class WalkerTalkerImpl extends BaseEntityImpl
{
    private Entity e;
    private EachTurnEntityImpl eachTurnEntityImpl;
    private InteractingEntityImpl interactingEntityImpl;

    public WalkerTalkerImpl(Entity e) {
        super();
        this.e = e;
        eachTurnEntityImpl = new EachTurnEntityImpl(e);
        interactingEntityImpl = new InteractingEntityImpl(e);
    }

    public EachTurnEntityImpl getEachTurnEntityImpl() {
        return eachTurnEntityImpl;
    }

    public InteractSupport getInteractSupport() {
        return interactingEntityImpl.getInteractSupport();
    }

    @Override
    public void gameStarting(Entity e) {
        eachTurnEntityImpl.gameStarting(e);
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        return interactingEntityImpl.getActions(e);
    }

    @Override
    public boolean processAction(Entity e, MMActions.Action action) {
        return interactingEntityImpl.processAction(e, action);
    }

    @Override
    public Object getState(Entity e) {
        return interactingEntityImpl.getState(e);
    }

    @Override
    public void restoreState(Entity e, Object state) {
        interactingEntityImpl.restoreState(e, state);
    }
}
