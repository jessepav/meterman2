package com.illcode.meterman2.ui;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.model.Game;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface between the UI and the rest of the game system.
 */
public interface UIHandler
{
    /** Called when the UI has finished initialization. */
    void uiInitialized();

    /** Return true if a game is currently active. */
    boolean isGameActive();

    /** Return a list of the names of the games available. */
    List<String> getGameNames();

    /** Called by when the user selects the "About..." menu item.*/
    void aboutMenuClicked();

    /** Called when the user invokes a debug command.  */
    void debugCommand(String cmd);

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
     * @param id entity ID of selected item, or null if the selection was cleared
     */
    void entitySelected(String id);

    /**
     * Called when an entity action is selected.
     * @param action action
     */
    void entityActionSelected(Action action);

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

    /** Adds text to the game transcript. Returns the same UIHandler instance so you can chain calls to
     *  this method. */
    UIHandler transcribe(String text);
}
