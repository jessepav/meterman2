package com.illcode.meterman2.event;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.model.Entity;

/**
 * A GameActionListener is notified when the player chooses an entity action.
 */
public interface GameActionListener extends GameEventHandler
{
    /**
     * Called when an action is sent to the selected entity. It may be called twice per action, once before
     * the action has actually been processed, and once after, if not interrupted by another
     * GameActionListener or {@link Entity#processAction(Action)}.
     * @param action action
     * @param e selected entity
     * @param beforeAction true if the method is being called before the action has reached the entity;
     *                     false if it is called after (i.e. the entity did not handle the action itself).
     * @return true to indicate that this listener processed the action, and to prevent further processing;
     *         false to continue the processing chain.
     */
    boolean processAction(Action action, Entity e, boolean beforeAction);

    /**
     * Called at the end of the action-processing chain regardless if the chain was interrupted
     * (i.e. handled) by a listener or the entity.
     * @param action action
     * @param e selected entity
     * @param actionHandled true if the action was processed (by a listener or the entity returning true
     *          from its {@code processAction()} method.)
     * @return true to suppress the normal "Nothing much happened" message if actionHandled is false.
     */
    boolean postAction(Action action, Entity e, boolean actionHandled);
}
