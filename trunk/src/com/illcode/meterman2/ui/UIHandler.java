package com.illcode.meterman2.ui;

import com.illcode.meterman2.model.Game;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface between the UI and the rest of the game system.
 */
public interface UIHandler
{
    /** Return true if a game is currently active. */
    boolean isGameActive();

    /** Called by when the user selects the "About..." menu item.*/
    void aboutMenuClicked();

    /** Called when the user invokes a debug command.  */
    void debugCommand(String cmd);

    /** Return a list of the names of the games available. */
    List<String> getGameNames();

    /**
     * Get the description associated with a given game name.
     * @param gameName the name of the game whose description to get. This will either be {@code null}
     *      or one of the names returned by {@link #getGameNames()}.
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
     * Called when the user selects an item from the room or inventory entity lists.
     * @param id entity ID of selected item
     */
    void entitySelected(String id);

    /**
     * Called when an entity action is selected.
     * @param action action name
     */
    void entityActionSelected(String action);

    /**
     * Called when an exit button is selected.
     * @param buttonPos one of the button positions defined in {@link UIConstants}
     */
    void exitSelected(int buttonPos);

    /** Enable or disable music. */
    void setMusicEnabled(boolean enabled);

    /** Return true if music is enabled in the game system. */
    boolean isMusicEnabled();

    /** Enable or disable sound. */
    void setSoundEnabled(boolean enabled);

    /** Return true if sound is enabled in the game system. */
    boolean isSoundEnabled();

    /** Set whether we should always "Look" when entering a room, even if it's been visited before. */
    void setAlwaysLook(boolean alwaysLook);

    /** Return true if the game system indicates we should always "Look" when entering a room. */
    boolean isAlwaysLook();
}
