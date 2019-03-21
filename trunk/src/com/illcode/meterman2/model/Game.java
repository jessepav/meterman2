package com.illcode.meterman2.model;

import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.event.GameEventHandler;

import java.util.Map;

/**
 * An instance of a particular game.
 * <p/>
 * The methods of the implementation are called in different patterns depending if we are starting a new
 * game or loading a saved game.
 * <p/>
 * For new games, we call methods in this order:
 * <ol>
 *     <li>getName()</li>
 *     <li>init()</li>
 *     <li>getGameStateMap()</li>
 *     <li>constructWorld()</li>
 *     <li>getEntityIdMap()</li>
 *     <li>getRoomIdMap()</li>
 *     <li>getPlayer()</li>
 *     <li>getStartingRoom()</li>
 *     <li>registerInitialGameHandlers()</li>
 *     <li>start()</li>
 * </ol>
 * For loaded games, we call methods in this order:
 * <ol>
 *     <li>getName()</li>
 *     <li>init()</li>
 *     <li>setGameStateMap()</li>
 *     <li>constructWorld()</li>
 *     <li>getEntityIdMap()</li>
 *     <li>getRoomIdMap()</li>
 *     <li>getEventHandler()</li>
 *     <li>start()</li>
 * </ol>
 * <blockquote>
 *   setGameStateMap() is called before constructWorld() so that game objects can hold valid references
 *   to the game state objects when they're constructed.
 *   <p/>
 *   Note that the entity and room ID maps should have the same key-set when the game was saved and when
 *   constructWorld() returns. If the game creates new entities or rooms during play, it should record
 *   that fact in one of the game state objects, so that constructWorld() can instantiate the appropriate
 *   objects when it's called upon load.
 * </blockquote>
 * The other methods do not participate in the process of starting or loading a game.
 * <hr/>
 * An instance of this class is constructed via reflection, as though the no-arg constructor were invoked.
 */
public interface Game
{
    /** The name of the game, as displayed in the "New Game" UI. It must match
     *  the name given in the game's definition file. */
    String getName();

    /**
     * Called when a game is started or loaded. A game should, in this method:
     * <ul>
     *     <li>add image and sound mappings</li>
     *     <li>register game-specific actions and attributes</li>
     *     <li>add its {@code XBundle}(s) to the system group</li>
     *     <li>call {@link MMScript#evalGameScript(java.lang.String)} to import packages and classes and
     *     define any methods used by scripts.</li>
     * </ul>
     */
    void init();

    /**
     * Free any allocated resources other than images and sounds (which will be freed
     * automatically). Called when the GameManager is unloading the current game instance.
     */
    void dispose();

    /**
     * Called at the start of a new game to retrieve a mapping of the game-state objects used by scripts and
     * templates. All global state should be kept in one of these objects so that it is persisted across game
     * sessions and available to scripts and templates.
     * <p/>
     * Game state objects should be POJOs that contain only standard Java types: primitives and their wrapper
     * types, Strings, lists, maps, and sets. All fields and methods should be public and non-static. For
     * collections, one should use concrete classes rather than interfaces, to ensure that the actual type
     * of the data structure is serializeable by standard serializers.
     * <p/>
     * The map keys will be used as the names under which these objects will be inserted in the scripting
     * namespace and template data model. Keys with a leading underscore are reserved for use by the system,
     * as well as "entity" and "room", for the selected entity and current room, respectively; and "args",
     * for text sources with argument lists.
     */
    Map<String,Object> getGameStateMap();

    /**
     * Called when a game is loaded, so that the game can retain references to game-state objects as they
     * were at the point when the game was saved.
     * @param gameStateMap map whose keys and values (state-objects) were initially returned by
     *      {@link #getGameStateMap()}, though the data in the state-objects may have been changed
     *      during the course of play.
     */
    void setGameStateMap(Map<String,Object> gameStateMap);

    /**
     * Instantiate all rooms and entities as they will be at the start of a game. Called both for new
     * and loaded games. It is only after calling this method that {@link #getEntityIdMap()} and {@link
     * #getRoomIdMap()} will return valid values.
     */
    void constructWorld();

    /**
     * Return a mapping from entity IDs to the actual Entity instances for all entities in the game.
     * This map's contents may change throughout the course of the game.
     */
    Map<String,Entity> getEntityIdMap();

    /**
     * Return a mapping from room IDs to the actual Room instances for all rooms in the game.
     * This map's contents may change throughout the course of the game.
     */
    Map<String,Room> getRoomIdMap();

    /**
     * Called at the start of a new game, after {@code constructWorld()}, to retrieve the player character,
     * who should already be have his/her inventory in hand.
     */
    Player getPlayer();

    /** Called at the start of a new game to get the starting room. */
    Room getStartingRoom();

    /**
     * Called at the start of a new game to register any initial game handlers. It is not
     * called when a game is loaded, but rather whichever handlers were registered at the
     * time the game was saved are retrieved via {@link #getEventHandler(String)} and
     * added to the appropriate notification lists.
     */
    void registerInitialGameHandlers();

    /**
     * Returns the handler with a given ID. This method is used upon loading a game, so that the
     * engine can query the game for listeners by id and add them again, rather than having to
     * serialize the handler instances themselves.
     * @param id handler id as returned by {@link GameEventHandler#getHandlerId()}.
     * @return an instance of the corresponding handler
     */
    GameEventHandler getEventHandler(String id);

    /**
     * Called when a game is started, just before the initial "Look" is performed.
     * @param newGame true if this is a new game, false if we're resuming a saved game.
     */
    void start(boolean newGame);

    /** Called when the user selects "About..." in the UI  */
    void about();

    /**
     * Reload an entity from its XBundle.
     * @param id entity ID
     * @return true on success
     */
    boolean reloadEntity(String id);

    /**
     * Reload room from its XBundle.
     * @param id room ID
     * @return true on success
     */
    boolean reloadRoom(String id);

    /**
     * The UI provides a facility for the implementor to enter debug commands, which will
     * be passed along to the game using this method. These commands can be used to,
     * for instance, jump to a later part of the game, move entities around, etc.
     * @param command debug command
     */
    void debugCommand(String command);
}
