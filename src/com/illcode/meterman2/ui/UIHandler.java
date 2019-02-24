package com.illcode.meterman2.ui;

import com.illcode.meterman2.model.Game;

/**
 * Interface defining callbacks that the UI will make into the game system.
 */
public interface UIHandler
{
    /** Returns true if a game is currently active. */
    boolean isGameActive();

    /** Return the active Game, or null if no game is active. */
    Game getGame();

    /**
     * Called when an action is invoked.
     * @param action action name
     */
    void entityActionSelected(String action);

    /** Called when the user invokes a debug command.  */
    void debugCommand(String cmd);

    /**
     * Called when the user selects an item from the room or inventory entity lists.
     * @param id entity ID of selected item
     */
    void entitySelected(String id);

    /**
     * Get the description associated with a given game name.
     * @param gameName the name of the game whose description to get (may be null).
     * @return description of the game. If {@code gameName} is null or doesn't exist,
     *      generic "choose a game" text should be returned.
     */
    String getGameDescription(String gameName);
}
