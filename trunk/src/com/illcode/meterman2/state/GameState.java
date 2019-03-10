package com.illcode.meterman2.state;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.model.EntityContainer;
import com.illcode.meterman2.model.Game;
import com.illcode.meterman2.ui.UIConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains all the state objects, listener lists, entity placements, etc. needed
 * to save and restore a game's state.
 * <p/>
 * We use concrete class types here rather than interfaces so that serialization and deserialization
 * are predictable.
 */
public final class GameState
{
    /** The game state objects as returned by {@link Game#getGameStateObjects()} when the game
     *  was first started. */
    public HashMap<String,Object> gameStateObjects;

    /**
     * Used to persist the registered event listeners in the GameManager.
     * <p/>
     * Each handler list (ex. turnListeners) is stored in an appropriately named key ("turnListeners"),
     * with the value being a list of the IDs of the registered handlers of that type.
     */
    public HashMap<String,List<String>> gameHandlers;

    /** State of all entities. */
    public HashMap<String,EntityState> entityState;

    /** Map from room ID to the room's attributes. */
    public HashMap<String,AttributeSet> roomAttributes;

    /** The names of all the attributes (and implicitly in their indices, their attribute
     *  numbers) registered at the time this game was saved. */
    public String[] attributeNames;

    /** The ID of the container where the player resides. */
    public String playerLocation;

    /** The IDs of the entities the player has equipped. */
    public String[] equippedEntityIds;

    public static final class EntityState
    {
        /** The container where the entity is located, or null. */
        String containerId;

        /** @see EntityContainer#getContainerType() */
        int containerType;

        /** Entity attributes. */
        AttributeSet attributes;
    }

    public static final class RoomState
    {
        /** Room attributes. */
        AttributeSet attributes;

        /** IDs of the rooms (or null) for each of the {@link UIConstants#NUM_EXIT_BUTTONS} positions. */
        String[] exitRoomIds;

        /** Exit labels (or null) for each of the {@link UIConstants#NUM_EXIT_BUTTONS} positions. */
        String[] exitLabels;
    }
}
