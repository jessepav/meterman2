package com.illcode.meterman2.state;

import com.illcode.meterman2.AttributeSet;
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

    /** State of all entities. */
    public Map<String,EntityState> entityState;

    /** Map from room ID to the room's attributes. */
    public Map<String,AttributeSet> roomAttributes;

    /** The number of system attributes that were registered when this game was saved.
     *  If the number of system attributes is different at the time it is loaded, we
     *  need to shift around the attribute numbers in our restored attribute sets to compensate. */
    public int numSystemAttributes;

    /** The ID of the container where the player resides. */
    public String playerLocation;

    /** The IDs of the entities the player has equipped. */
    public Set<String> equippedEntityIds;

    public static final class EntityState
    {
        /** The container where the entity is located, or null. */
        String containerId;

        /** @see EntityContainer#getContainerType() */
        int containerType;

        /** Entity attributes. */
        AttributeSet attributes;
    }
}
