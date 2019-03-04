package com.illcode.meterman2.model;

import com.illcode.meterman2.MMAttributes.AttributeSet;

import java.util.List;

/**
 * The "interface" through which the game system and UI interacts with entities.
 * <p/>
 * Entity is a class rather than an interface because it supports method delegation and attributes; all
 * other implementation is handled by an instance of {@link EntityImpl}. It is that interface, and its base
 * implementation {@link BaseEntityImpl}, that specialized entities will usually extend.
 */
public class Entity
{
    private EntityImpl impl;

    /** Return the entity implementation instance used by this Entity. */
    public EntityImpl getImpl() {
        return impl;
    }

    /** Set the entity implementation instance used by this Entity. */
    public void setImpl(EntityImpl impl) {
        this.impl = impl;
    }

    /** Return this entity's attributes. */
    public AttributeSet getAttributes() {
        return null;
    }

    /** Return the name of this entity */
    public String getName() {
        return null;
    }

    /** Return the indefinite article to be used when referring to this entity.
     *  A null return value causes the system to make its best guess. */
    public String getIndefiniteArticle() {
        return null;
    }

    /** Return the entity description. */
    public String getDescription() {
        return null;
    }

    /**
     * Called on each entity in a room when a look command is issued. While the implementation is free to do
     * anything, generally it will call {@code GameManager.queueLookText()} to add text to
     * the room description printed.
     */
    public void lookInRoom() {

    }

    /**
     * Called when the entity enters scope. This can occur when:
     * <ol>
     *     <li>The player moves into the room where this entity resides</li>
     *     <li>The entity is added to the current room</li>
     *     <li>The entity is added to the player inventory from somewhere outside the current room</li>
     * </ol>
     */
    public void enterScope() {

    }

    /**
     * Called when the entity is exiting scope. This can occur when:
     * <ol>
     *     <li>The player is exiting the room where the entity resides</li>
     *     <li>The entity is being moved from the current room to somewhere other than
     *         the player's inventory</li>
     *     <li>The entity is being moved from the player's inventory to somewhere other than
     *         the current room</li>
     * </ol>
     * Note that this method is called before any of the above actions take place, so it
     * still has a valid place in the world graph.
     */
    public void exitingScope() {

    }

    /** Called when the entity is moved to the player inventory */
    public void taken() {

    }

    /** Called when the entity is removed from the player inventory */
    public void dropped() {

    }

    /**
     * Returns the room  where this entity is found. If the entity is held in player inventory,
     * this returns the current player room.
     * @return the room, or null if not in a room or inventory
     */
    public Room getRoom() {
        return null;
    }

    /**
     * Sets the room where this entity resides, or null if it doesn't reside anywhere.
     * @param room
     */
    public void setRoom(Room room) {

    }

    /**
     * Returns a list of extra actions to be shown in the UI. Never returns null.
     */
    List<String> getActions() {
        return null;
    }

    /**
     * The player invoked an action on this entity from the UI
     * @param action action name
     * @return true if the entity processed the action itself, false to continue
     *              through the processing chain
     */
    boolean processAction(String action) {
        return false;
    }
}
