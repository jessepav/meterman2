package com.illcode.meterman2.model;

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
     * Return a mapping from entity IDs to the actual Entity instances for all entities in the game.
     */
    Map<String,Entity> getEntityIdMap();

    /**
     * Return a mapping from room IDs to the actual Room instances for all rooms in the game.
     */
    Map<String,Room> getRoomIdMap();

    /**
     * Return a mapping of the game-state objects used by scripts and templates. All state which
     * can change through the course of a game should be kept in one of these objects, rather than
     * in random instance variables of entities, rooms, listeners, etc. because it is only these
     * objects which will be persisted and loaded across game sessions.
     * <p/>
     * The map keys will be used as the names under which these objects will be inserted in the
     * scripting namespace and template data model.
     */
    Map<String,Object> getGameStateObjects();

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
