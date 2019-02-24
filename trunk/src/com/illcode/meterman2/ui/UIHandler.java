package com.illcode.meterman2.ui;

import com.illcode.meterman2.model.Game;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface defining callbacks that the UI will make into the game system.
 */
public interface UIHandler
{
    /** Returns true if a game is currently active. */
    boolean isGameActive();

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

    /** Return a list of the names of the games available. */
    List<String> getGameNames();

    /**
     * Get the description associated with a given game name.
     * @param gameName the name of the game whose description to get (may be null).
     * @return description of the game. If {@code gameName} is null or doesn't exist,
     *      generic "choose a game" text should be returned.
     */
    String getGameDescription(String gameName);

    /**
     * Start a new game.
     * @param gameName the name of the game to start.
     */
    void newGame(String gameName);

    /**
     * Load a game from an input stream (i.e. a save file).
     * @param in InputStream of serialized game state
     */
    void loadGameState(InputStream in);

    /**
     * Save the game state to an output stream.
     * @param out output stream to which to write the serialized game state
     */
    void saveGameState(OutputStream out);

    /** Called when the Look command is selected. */
    void lookCommand();

    /** Called when the Wait command is selected. */
    void waitCommand();

    /**
     * Called when an exit button is selected.
     * @param buttonPos one of the button positions defined in {@link UIConstants}
     */
    void exitSelected(int buttonPos);

    /** Set music enabled. */
    void setMusicEnabled(boolean enabled);

    /** Set sound enabled. */
    void setSoundEnabled(boolean enabled);

    /** Set whether we should always "Look" when entering a room, even if it's been visited before. */
    void setAlwaysLook(boolean alwaysLook);

    /** Called by when the user selects the "About..." menu item.*/
    void aboutMenuClicked();
}
