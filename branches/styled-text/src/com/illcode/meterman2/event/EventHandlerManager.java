package com.illcode.meterman2.event;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;

import java.util.*;

/**
 * Support class to handle registration and event firing for <tt>GameEventHandler</tt>S.
 */
public final class EventHandlerManager
{
    private LinkedList<GameActionListener> gameActionListeners;
    private LinkedList<PlayerMovementListener> playerMovementListeners;
    private LinkedList<TurnListener> turnListeners;
    private LinkedList<EntityActionsProcessor> entityActionsProcessors;
    private LinkedList<EntitySelectionListener> entitySelectionListeners;
    private LinkedList<OutputTextProcessor> outputTextProcessors;
    private LinkedList<LookListener> lookListeners;

    private ArrayList<GameEventHandler> fireList;

    private Map<String,List<? extends GameEventHandler>> eventHandlerMap;

    public EventHandlerManager() {
        gameActionListeners = new LinkedList<>();
        playerMovementListeners = new LinkedList<>();
        turnListeners = new LinkedList<>();
        entityActionsProcessors = new LinkedList<>();
        entitySelectionListeners = new LinkedList<>();
        outputTextProcessors = new LinkedList<>();
        lookListeners = new LinkedList<>();

        fireList = new ArrayList<>(16);
    }

    /**
     * Clears our registration lists.
     */
    public void clearListenerLists() {
        gameActionListeners.clear();
        playerMovementListeners.clear();
        turnListeners.clear();
        entityActionsProcessors.clear();
        entitySelectionListeners.clear();
        outputTextProcessors.clear();
        lookListeners.clear();
    }

    /**
     * Return a map from the name of each registration list (ex. "playerMovementListeners") to
     * the registered handlers in that list.
     * @return handler map
     */
    public Map<String, List<? extends GameEventHandler>> getEventHandlerMap() {
        if (eventHandlerMap == null) {
            eventHandlerMap = new HashMap<>();
            eventHandlerMap.put("gameActionListeners", gameActionListeners);
            eventHandlerMap.put("playerMovementListeners", playerMovementListeners);
            eventHandlerMap.put("turnListeners", turnListeners);
            eventHandlerMap.put("entityActionsProcessors", entityActionsProcessors);
            eventHandlerMap.put("entitySelectionListeners", entitySelectionListeners);
            eventHandlerMap.put("outputTextProcessors", outputTextProcessors);
            eventHandlerMap.put("lookListeners", lookListeners);
        }
        return eventHandlerMap;
    }

    /**
     * Fire a game starting event.
     * @param newGame true if this is a new game, false if we're resuming a saved game.
     */
    public void fireGameStarting(boolean newGame) {
        for (List<? extends GameEventHandler> handlerList : getEventHandlerMap().values()) {
            fireList.addAll(handlerList);
            for (GameEventHandler handler : fireList)
                handler.gameHandlerStarting(newGame);
            fireList.clear();
        }
    }

    /**
     * Adds a GameActionListener to be called when a game action is processed. If the listener's
     * {@code processAction()} method returns true, further action processing will be bypassed.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addGameActionListener(GameActionListener l) {
        if (l != null && !gameActionListeners.contains(l))
            gameActionListeners.addFirst(l);
    }

    /**
     * Removes a GameActionListener from the action notification list.
     * @param l listener to remove
     */
    public void removeGameActionListener(GameActionListener l) {
        gameActionListeners.remove(l);
    }

    /**
     * Notifies all registered {@code GameActionListener}S that an action is being, or has been, processed.
     * @param action action
     * @param e selected entity
     * @param beforeAction true if the method is being called before the action has been
     *          processed; false otherwise.
     * @return true if any GameActionListener interrupted the chain by returning true,
     *         and thus normal action processing should be skipped.
     */
    public boolean fireGameAction(MMActions.Action action, Entity e, boolean beforeAction) {
        boolean retVal = false;
        fireList.addAll(gameActionListeners);
        for (GameEventHandler h : fireList) {
            if (((GameActionListener) h).processAction(action, e, beforeAction)) {
                retVal = true;
                break;
            }
        }
        fireList.clear();
        return retVal;
    }

    /**
     * Notifies all registered <tt>GameActionListener</tt>S that action-processing has finished.
     * @return true if any GameActionListener indicated that the default "Nothing much happened" message
     *          should be suppressed.
     */
    public boolean firePostAction(MMActions.Action action, Entity e, boolean actionHandled) {
        boolean suppressMessage = false;
        fireList.addAll(gameActionListeners);
        for (GameEventHandler h : fireList)
            suppressMessage = ((GameActionListener) h).postAction(action, e, actionHandled) || suppressMessage;
        fireList.clear();
        return suppressMessage;
    }

    /**
     * Notifies all registered {@code GameActionListener}S that an object action is going to be performed.
     * @param object additional object of the action
     * @param action action
     * @param selectedEntity selected entity
     * @return true if any GameActionListener interrupted the chain by returning true, and thus that
     *      the action should be blocked.
     */
    public boolean fireObjectAction(Entity object, MMActions.Action action, Entity selectedEntity) {
        boolean retVal = false;
        fireList.addAll(gameActionListeners);
        for (GameEventHandler h : fireList)
            if (((GameActionListener) h).objectAction(object, action, selectedEntity)) {
                retVal = true;
                break;
            }
        fireList.clear();
        return retVal;
    }

    /**
     * Adds a PlayerMovementListener to be called when the player moves. The listener may return true from
     * its {@link PlayerMovementListener#playerMove} method to halt further movement processing.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added listener will be
     * notified before previously added listeners.
     * @param l listener to add
     */
    public void addPlayerMovementListener(PlayerMovementListener l) {
        if (l != null && !playerMovementListeners.contains(l))
            playerMovementListeners.addFirst(l);
    }

    /**
     * Removes a PlayerMovementListener from our notification list.
     * @param l listener to remove
     */
    public void removePlayerMovementListener(PlayerMovementListener l) {
        playerMovementListeners.remove(l);
    }

    /**
     * Notifies registered {@code PlayerMovementListener}S that the player is moving or has moved.
     * @param from room player moves from
     * @param to room player moves to
     * @param beforeMove true if being called before player movement has occurred; false otherwise.
     * @return true if any PlayerMovementListener interrupted the chain by returning true.
     */
    public boolean firePlayerMovement(Room from, Room to, boolean beforeMove) {
        boolean retVal = false;
        fireList.addAll(playerMovementListeners);
        for (GameEventHandler h : fireList) {
            if (((PlayerMovementListener) h).playerMove(from, to, beforeMove)) {
                retVal = true;
                break;
            }
        }
        fireList.clear();
        return retVal;
    }

    /**
     * Adds a TurnListener to be notified when the turn cycles to the next.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added listener will be
     * notified before previously added listeners.
     * @param l listener to add
     */
    public void addTurnListener(TurnListener l) {
        if (l != null && !turnListeners.contains(l))
            turnListeners.addFirst(l);
    }

    /**
     * Removes a TurnListener from the turn-cycle notification list.
     * @param l listener to remove
     */
    public void removeTurnListener(TurnListener l) {
        turnListeners.remove(l);
    }

    /** Notifies registered {@code TurnListener}S that we have reached the cycle of turns */
    public void fireTurn() {
        fireList.addAll(turnListeners);
        for (GameEventHandler h : fireList)
            ((TurnListener) h).turn();
        fireList.clear();
    }

    /**
     * Adds an EntityActionsProcessor.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addEntityActionsProcessor(EntityActionsProcessor l) {
        if (l != null && !entityActionsProcessors.contains(l))
            entityActionsProcessors.addFirst(l);
    }

    /**
     * Removes a EntityActionsProcessor.
     * @param l listener to remove
     */
    public void removeEntityActionsProcessor(EntityActionsProcessor l) {
        entityActionsProcessors.remove(l);
    }

    /**
     * Notifies registered {@code EntityActionsProcessor}S that an entity's action list is
     * being generated.
     * @param e entity
     * @param actions the mutable list of actions that should be shown in the UI,
     *                which each listener may modify.
     */
    public void fireProcessEntityActions(Entity e, List<MMActions.Action> actions) {
        fireList.addAll(entityActionsProcessors);
        for (GameEventHandler h : fireList)
            ((EntityActionsProcessor) h).processEntityActions(e, actions);
        fireList.clear();
    }

    /**
     * Adds a EntitySelectionListener.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addEntitySelectionListener(EntitySelectionListener l) {
        if (l != null && !entitySelectionListeners.contains(l))
            entitySelectionListeners.addFirst(l);
    }

    /**
     * Removes a EntitySelectionListener.
     * @param l listener to remove
     */
    public void removeEntitySelectionListener(EntitySelectionListener l) {
        entitySelectionListeners.remove(l);
    }

    /**
     * Notifies registered {@code EntitySelectionListener}S that an entity has been selected.
     * @param e selected entity
     */
    public void fireEntitySelected(Entity e) {
        fireList.addAll(entitySelectionListeners);
        for (GameEventHandler h : fireList)
            if (((EntitySelectionListener) h).entitySelected(e))
                break;
        fireList.clear();
    }

    /**
     * Add an OutputTextProcessor to our notification list.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added listener will be
     * notified before previously added listeners.
     * @param p processor to add
     */
    public void addOutputTextProcessor(OutputTextProcessor p) {
        if (p != null && !outputTextProcessors.contains(p))
            outputTextProcessors.addFirst(p);
    }

    /**
     * Remove a OutputTextProcessor from our notification list.
     * @param p processor to add
     */
    public void removeOutputTextProcessor(OutputTextProcessor p) {
        outputTextProcessors.remove(p);
    }

    /**
     * Notifies registered <tt>OutputTextProcessor</tt>s that output text
     * has been gathered and will be displayed in the UI.
     * @param sb the StringBuilder containing the text to be shown
     */
    public void fireOutputTextReady(StringBuilder sb) {
        fireList.addAll(outputTextProcessors);
        for (GameEventHandler h : fireList)
            ((OutputTextProcessor) h).outputTextReady(sb);
        fireList.clear();
    }

    /** Add a look-listener to the front of our notification list. */
    public void addLookListener(LookListener l) {
        if (l != null && !lookListeners.contains(l))
            lookListeners.addFirst(l);
    }

    /** Remove a look-listener from our notification list. */
    public void removeLookListener(LookListener l) {
        lookListeners.remove(l);
    }

    /**
     * Notifies registered look-listeners that a Look command has been performed.
     * @param currentRoom the room where the player is looking
     */
    public void fireLookPerformed(Room currentRoom) {
        fireList.addAll(lookListeners);
        for (GameEventHandler h : fireList)
            ((LookListener) h).lookInRoom(currentRoom);
        fireList.clear();
    }
}
