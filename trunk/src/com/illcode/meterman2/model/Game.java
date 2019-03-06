package com.illcode.meterman2.model;

import com.illcode.meterman2.event.GameEventHandler;

import java.util.Map;

/**
 * An instance of a particular game.
 */
public interface Game
{
    /** The name of the game, as displayed in the "New Game" UI. It must match
     *  the name given in the game's definition file. */
    String getName();

    /**
     * Called when a game is started or loaded. A game should, in this method, load any resources (images,
     * sound, bundles) it wants immediately or game-globally available.
     * <p/>
     * It should also register game-specific actions and add its {@code XBundle}S to the system group.
     */
    void init();

    /**
     * Free any allocated resources other than images and sounds (which will be freed
     * automatically). Called when the GameManager is unloading the current game instance.
     */
    void dispose();

    /**
     * Instantiate all rooms and entities, and link the rooms together. This method
     * does not place entities into rooms (and other entities): that's the job of
     * {@link #setInitialEntityPlacements()}. Called both for new and loaded games.
     */
    void constructWorld();

    /**
     * Place entities into rooms and other containers (including the player inventory) at
     * the start of a new game. This method is not called when a game is loaded, but rather
     * the entities are put where they were when the game was saved.
     */
    void setInitialEntityPlacements();

    /**
     * Called at the start of a new game to register any initial game handlers. It is not
     * called when a game is loaded, but rather whichever handlers were registered at the
     * time the game was saved are retrieved via {@link #getEventHandler(String)} and
     * added to the appropriate notification lists.
     */
    void registerInitialGameHandlers();

    /**
     * Called at the start of a new game, after {@code constructWorld()} and {@code setInitialEntityPlacements()},
     * to retrieve the player character, who should already be placed in the starting room with his/her
     * inventory in hand.
     */
    Player getPlayer();

    /**
     * Called at the start of a new game to retrieve a mapping of the game-state objects used by scripts and
     * templates. All state which can change through the course of a game should be kept in one of these
     * objects, rather than in random instance variables of entities, rooms, listeners, etc. because it is
     * only these objects which will be persisted and loaded across game sessions.
     * <p/>
     * The map keys will be used as the names under which these objects will be inserted in the scripting
     * namespace and template data model.
     */
    Map<String,Object> getGameStateObjects();

    /**
     * Called when a game is loaded, so that the game can retain references to game-state objects as they
     * were at the point when the game was saved.
     * @param gameStateMap map whose keys and values (state-objects) were initially returned by
     *      {@link #getGameStateObjects()}, though the data in the state-objects may have been changed
     *      during the course of play.
     */
    void setGameStateObjects(Map<String,Object> gameStateMap);

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
     * Returns the handler with a given ID. This method is used upon loading a game, so that the
     * engine can query the game for listeners by id and add them again, rather than having to
     * serialize the handler instances themselves.
     * @param id handler id as returned by {@link GameEventHandler#getId()}.
     * @return an instance of the corresponding handler
     */
    GameEventHandler getEventHandler(String id);

    /** Called when the user selects "About..." in the UI  */
    void about();

    /**
     * The UI provides a facility for the implementor to enter debug commands, which will
     * be passed along to the game using this method. These commands can be used to,
     * for instance, jump to a later part of the game, move entities around, etc.
     * @param command debug command
     */
    void debugCommand(String command);
}
