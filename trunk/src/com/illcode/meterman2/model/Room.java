package com.illcode.meterman2.model;

import com.illcode.meterman2.MMAttributes.AttributeSet;
import com.illcode.meterman2.ui.UIConstants;

import java.util.List;
import java.util.Map;

/**
 * The "interface" through which the game system and UI interacts with rooms.
 * <p/>
 * Room is a class rather than an interface because it supports method delegation and attributes; all
 * other implementation is handled by an instance of {@link RoomImpl}. It is that interface, and its base
 * implementation {@link BaseRoomImpl}, that specialized rooms will usually extend.
 */
public class Room
{
    protected String id;
    protected RoomImpl impl;

    /** Construct a room with the given ID. */
    public Room(String id) {
        this.id = id;
    }

    /** Return the room implementation instance used by this Room. */
    public RoomImpl getImpl() {
        return impl;
    }

    /** Return the unique ID of this room. */
    public String getId() {
        return null;
    }

    /** Set the room implementation instance used by this Room. */
    public void setImpl(RoomImpl impl) {
        this.impl = impl;
    }

    /** Return this room's attributes. */
    public AttributeSet getAttributes() {
        return null;
    }

    /** Returns the full name of the room. */
    public String getName() {
        return null;
    }

    /** Returns a potentially shorter version of the name, to be used in Exit buttons.
     *  This method may return a different name depending on whether the room has been visited. */
    public String getExitName() {
        return null;
    }

    /** Returns the text to be displayed when the player enters the room or clicks "Look". */
    public String getDescription() {
        return null;
    }

    /**
     * Return the room associated with a given exit direction. This method is used in the world model
     * to get the actual room to which an exit leads.
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @return the room that is found when exiting this room in the given direction, or null if no
     *         exit is possible in that direction.
     * @see #getExitLabel(int)
     */
    public Room getExit(int direction) {
        return null;
    }

    /**
     * Return the text that should be shown on the UI button for a given direction.
     * @param direction one of the button constants in {@link UIConstants}
     * @return the text that should be shown on the respective UI button, or null if the button should
     *         be hidden
     * @see #getExit(int)
     */
    public String getExitLabel(int direction) {
        return null;
    }

    /**
     * Returns a list of the entities found in this room.
     */
    public List<Entity> getRoomEntities() {
        return null;
    }

    /**
     * Called when the player has entered the room.
     * @param fromRoom the room (possibly null) from which the player entered
     */
    public void entered(Room fromRoom) {
    }

    /**
     * Called as the player is exiting the room (but is still there).
     * @param toRoom the room the player is attempting to move to.
     * @return true to block the player from exiting, false to allow the exit (note that
     *          the exit may fail for other reasons)
     */
    public boolean exiting(Room toRoom) {
        return false;
    }

    /**
     * Called when the game is being saved. The room should store any mutable data that is not in one of
     * the game-state objects into {@code stateMap}, using a key that begins with {@code "<room ID>:"}.
     * @param stateMap map that will be serialized into the save file
     */
    public void saveState(Map<String,Object> stateMap) {

    }

    /**
     * Called when the game is being restored. Any entries that were put into it by <tt>saveState()</tt>
     * will be available.
     * @param stateMap map that was deserialized from the save file
     */
    public void restoreState(Map<String,Object> stateMap) {

    }
}
