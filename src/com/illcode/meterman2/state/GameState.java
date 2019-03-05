package com.illcode.meterman2.state;

import com.illcode.meterman2.model.Game;

import java.util.List;
import java.util.Map;

/**
 * Contains all the state objects, listener lists, entity placements, etc. needed
 * to save and restore a game's state.
 */
public final class GameState
{
    /** The game state objects as returned by {@link Game#getGameStateObjects()} when the game
     *  was first started. */
    public Map<String,Object> gameStateObjects;

    /**
     * Used to persist the registered event listeners in the GameManager.
     * <p/>
     * Each handler list (ex. turnListeners) is stored in an appropriately named key ("turnListeners"),
     * with the value being a list of the IDs of the registered handlers of that type.
     */
    public Map<String,List<String>> gameHandlers;

    /**
     * The map that will be passed to the <tt>saveState()</tt> and <tt>restoreState()</tt> methods
     * of Entity and Room.
     */
    public Map<String,Object> gameObjectState;
}
