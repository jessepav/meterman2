package com.illcode.meterman2.state;

import com.illcode.meterman2.model.EntityContainer;
import com.illcode.meterman2.model.Game;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /** A mapping from entity ID to room ID, for entities contained in rooms. */
    public Map<String,String> entitiesInRooms;

    /** A mapping from entity ID to entity ID, for entities contained in other entities. */
    public Map<String,String> entitiesInEntities;

    /** A list of the entity IDs that are in the player inventory. */
    public List<String> entitiesInInventory;

    /** The ID of the container where the player resides. */
    public String playerContainerId;

    /** The type of container represented by {@code playerContainerId}.
     *  @see EntityContainer#getContainerType() */
    public int playerContainerType;

    /** The IDs of the entities the player has equipped. Note that the entities in the player inventory
     *  will have their location (the player) saved when their own container IDs are saved.*/
    public Set<String> equippedEntityIds;
}
