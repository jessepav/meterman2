package com.illcode.meterman2.state;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.model.EntityContainer;
import com.illcode.meterman2.model.Game;
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
    /** The game state objects as returned by {@link Game#getGameStateObjects()} when the game
     *  was first started. */
    public HashMap<String,Object> gameStateObjects;

    /**
     * Used to persist the registered event listeners in the GameManager.
     * <p/>
     * Each handler list (ex. turnListeners) is stored in an appropriately named key ("turnListeners"),
     * with the value being an array of the IDs of the registered handlers of that type.
     */
    public HashMap<String,String[]> gameHandlers;

    /** Map from entity ID to its state. */
    public HashMap<String,EntityState> entityState;

    /** Map from room ID to its state. */
    public HashMap<String,RoomState> roomState;

    /**
     * The names of all the attributes (and implicitly in their indices, their attribute numbers) registered
     * at the time this game was saved. We can use this information to patch up the attribute numbers if the
     * attribute registration state is different when we're loaded from when we were saved.
     */
    public String[] attributeNames;

    public PlayerState playerState;

    /** Container class for the standard properties of entities. */
    public static final class EntityState
    {
        public String name;
        public String indefiniteArticle;

        /** The container where the entity is located, or null. */
        public String containerId;

        /** @see EntityContainer#getContainerType() */
        public int containerType;

        /** Entity attributes. */
        public AttributeSet attributes;
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
    }

    /**
     * Container class for the standard properties of the player.
     * <p/>
     * Note that the entities in the player inventory will have their {@code containerId} equal to {@code
     * Player.PLAYER_ID}, so we don't need to keep track of them here.
     */
    public static final class PlayerState
    {
        /** The ID of the container where the player resides. */
        public String containerId;

        /** The IDs of the entities the player has equipped. */
        public String[] equippedEntityIds;
    }
}
