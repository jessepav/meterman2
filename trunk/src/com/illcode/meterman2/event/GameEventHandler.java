package com.illcode.meterman2.event;

import com.illcode.meterman2.Game;

/**
 * The super-interface for all event listeners, processors, handlers etc.
 */
public interface GameEventHandler
{
    /**
     * Return the ID of this GameEventHandler. The ID is used to persist the active listeners when a game is
     * saved and subsequently loaded, without needing to serialize the actual handler objects. If the ID
     * starts with a '#', then the corresponding handler will not be persisted.
     */
    String getHandlerId();

    /**
     * Called when a game is saved to get a state object that will be persisted in the saved game file.
     * The object's class should be one of the standard POJO types descripted in {@link Game#getInitialGameStateMap()}
     * @return state object, or null to indicate no state needs to be saved
     */
    Object getHandlerState();

    /**
     * Called when a game is loaded to restore handler state.
     * @param state state object (possibly null) previously returned by <tt>getHandlerState()</tt>.
     */
    void restoreHandlerState(Object state);

    /**
     * Called immediately before {@link Game#start(boolean)}. The handler can
     * update itself or the UI as necessary.
     * @param newGame true if this is a new game, false if we're resuming a saved game.
     */
    void gameHandlerStarting(boolean newGame);
}
