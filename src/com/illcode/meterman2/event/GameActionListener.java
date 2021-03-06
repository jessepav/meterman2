package com.illcode.meterman2.event;

import com.illcode.meterman2.MMActions;
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
     * GameActionListener or {@link Entity#processAction(MMActions.Action)}.
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

    /**
     * Called during the processing of certain actions, like putting an item in a container, that
     * involve the selected entity (ex. the container) and an additional object (ex. the item being
     * put in).
     * @param object additional object of the action
     * @param action action
     * @param selectedEntity the selected entity
     * @return true to block the action, false to allow it to continue. If the method returns true,
     *      it should additionally print a message indicating why the action was blocked.
     */
    boolean objectAction(Entity object, Action action, Entity selectedEntity);
}
