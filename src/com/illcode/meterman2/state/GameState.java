package com.illcode.meterman2.state;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.event.GameEventHandler;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.Game;
import com.illcode.meterman2.model.GameObjectProperties;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.ui.UIConstants;

import java.util.HashMap;

/**
 * Contains all the state objects, listener lists, entity placements, etc. needed
 * to save and restore a game's state.
 * <p/>
 * We use concrete class types here rather than interfaces so that serialization and deserialization
 * are predictable.
 */
public final class GameState
{
    /** The version of the Meterman2 engine used to save this file. */
    public int engineVersion;

    /** The name of the game, as found in its definition file. */
    public String gameName;

    /** The version of the game that was running when we saved this file. */
    public String gameVersion;

    /** The game state map as returned by {@link Game#getInitialGameStateMap()} when the game
     *  was first started. */
    public HashMap<String,Object> gameStateMap;

    /** Names of attributes (and implicitly their indices) at the time the game was saved.
     *  We examine this list, along with the list at the time the game is restored, to see
     *  if we need to perform attribute index permutation. */
    public String[] attributeNames;

    /** Map from entity ID to its state. */
    public HashMap<String,EntityState> entityStateMap;

    /** Map from room ID to its state. */
    public HashMap<String,RoomState> roomStateMap;

    /** State of the player. */
    public PlayerState playerState;

    /**
     * Used to persist the registered event listeners in the GameManager.
     * <p/>
     * Each handler list (ex. turnListeners) is stored in an appropriately named key ("turnListeners"),
     * with the value being an array of the IDs of the registered handlers of that type.
     */
    public HashMap<String,String[]> gameHandlers;

    /** Persists the objects returned by {@link GameEventHandler#getHandlerState()}. */
    public HashMap<String,Object> handlerStateMap;

    /** Holds custom entity properties (keyed by entity ID).
        @see GameObjectProperties */
    public HashMap<String,HashMap<String,Object>> entityIdPropertyMap;

    /** Holds custom room properties (keyed by room ID).
        @see GameObjectProperties */
    public HashMap<String,HashMap<String,Object>> roomIdPropertyMap;

    /** The ID of the room where the action left off. */
    public String currentRoomId;

    /** The number of turns that have passed so far. */
    public int numTurns;

    /** Container class for the standard properties of entities. */
    public static final class EntityState
    {
        public String name;
        public String indefiniteArticle;

        /** Entity attributes. */
        public AttributeSet attributes;

        /** If non-null, then holds the entity IDs of the contents this entity. */
        public String[] contentIds;

        /** The object returned by {@link Entity#getState()} */
        public Object stateObj;
    }

    /** Container class for the standard properties of rooms. */
    public static final class RoomState
    {
        public String name;
        public String exitName;

        /** Room attributes. */
        public AttributeSet attributes;

        /** IDs of the rooms (or null) for each of the {@link UIConstants#NUM_EXIT_BUTTONS} positions. */
        public String[] exitRoomIds;

        /** Exit labels (or null) for each of the {@link UIConstants#NUM_EXIT_BUTTONS} positions. */
        public String[] exitLabels;

        /** If non-null, then holds the entity IDs of the contents of this room. */
        public String[] contentIds;

        /** The object returned by {@link Room#getState()} */
        public Object stateObj;
    }

    /**
     * Container class for the standard properties of the player.
     */
    public static final class PlayerState
    {
        /** The IDs of the entities the player has in inventory. */
        public String[] inventoryEntityIds;

        /** The IDs of the entities the player has equipped. */
        public String[] equippedEntityIds;
    }
}
