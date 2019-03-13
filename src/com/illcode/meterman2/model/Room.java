package com.illcode.meterman2.model;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.model.RoomImpl.RoomMethod;
import com.illcode.meterman2.ui.UIConstants;

import java.util.EnumSet;
import java.util.List;

import static com.illcode.meterman2.model.RoomImpl.RoomMethod.*;

/**
 * The base class through which the game system and UI interacts with rooms.
 * <p/>
 * Room itself handles its names, exits, method delegation, attributes, and containment; all other
 * implementation is handled by an instance of {@link RoomImpl}. It is that interface, and its base
 * implementation {@link BaseRoomImpl}, that specialized rooms will usually extend.
 */
public class Room implements EntityContainer
{
    // These comprise the standard properties of a room, and will be persisted.
    protected String id;
    protected String name;
    protected String exitName;
    private AttributeSet attributes;
    protected Room[] exits;
    protected String[] exitLabels;

    // These are behavioral, and are not persisted.
    protected RoomImpl impl;
    private RoomImpl delegate;
    private EnumSet<RoomMethod> delegateMethods;
    private ContainerSupport containerSupport;

    /** Construct a room with the given ID. */
    protected Room(String id, RoomImpl impl) {
        this.id = id;
        exits = new Room[UIConstants.NUM_EXIT_BUTTONS];
        exitLabels = new String[UIConstants.NUM_EXIT_BUTTONS];
        this.impl = impl;
        attributes = AttributeSet.create();
        containerSupport = new ContainerSupport(this);
    }

    /** Create a room with the given ID and a basic implemention. */
    public static Room create(String id) {
        return create(id, new BaseRoomImpl());
    }

    /** Create a room with the given ID and implemention. */
    public static Room create(String id, RoomImpl impl) {
        return new Room(id, impl);
    }

    /** Return the room implementation instance used by this Room. */
    public RoomImpl getImpl() {
        return impl;
    }

    /** Set the room implementation instance used by this Room. */
    public void setImpl(RoomImpl impl) {
        this.impl = impl;
    }

    /**
     * Set a delegate to which certain methods will be forwarded. This exists primarily to support
     * scripted methods, but may find other uses, such as consolidating logic for multiple rooms in one
     * "manager" class.
     * @param delegate the delegate implementation
     * @param delegateMethods a set indicating which methods should be forwarded
     */
    public void setDelegate(RoomImpl delegate, EnumSet<RoomMethod> delegateMethods) {
        this.delegate = delegate;
        this.delegateMethods = delegateMethods;
    }

    /** Remove the delegate. */
    public void clearDelegate() {
        delegate = null;
        delegateMethods = null;
    }

    /** Return the unique ID of this room. */
    public String getId() {
        return id;
    }

    /** Return this room's attributes, a mutable set that the caller can query and manipulate. */
    public final AttributeSet getAttributes() {
        return attributes;
    }

    //region -- implement EntityContainer
    public final int getContainerType() { return CONTAINER_ROOM; }
    public final String getContainerId() { return getId(); }
    public final Room getContainerAsRoom() { return this; }
    public final Entity getContainerAsEntity() { return null; }
    public final Player getContainerAsPlayer() { return null; }
    public final void addEntity(Entity e) { containerSupport.addEntity(e); }
    public final void clearEntities() { containerSupport.clearEntities(); }
    public final List<Entity> getEntities() { return containerSupport.getEntities(); }
    public final void removeEntity(Entity e) { containerSupport.removeEntity(e); }
    //endregion

    /** Returns the name of the room. */
    public String getName() {
        return name != null ? name : "[" + getId() + "]";
    }

    /** Set the name of the room. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns a potentially shorter version of the name, to be used in Exit buttons. */
    public String getExitName() {
        return exitName != null ? exitName : getName();
    }

    /** Set the version of the name to be used in Exit buttons. */
    public void setExitName(String exitName) {
        this.exitName = exitName;
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
        return exits[direction];
    }

    /**
     * Set the room associated with a given exit direction.
     * @param direction one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @param destination the room that is found when exiting this room in the given direction, or null if no
     *         exit is possible in that direction.
     */
    public void setExit(int direction, Room destination) {
        exits[direction] = destination;
    }

    /**
     * Return the text that should be shown on the UI button for a given direction.
     * @param direction one of the button constants in {@link UIConstants}
     * @return the text that should be shown on the respective UI button, or null if the button should
     *         be hidden
     */
    public String getExitLabel(int direction) {
        if (exitLabels[direction] != null)
            return exitLabels[direction];
        else if (exits[direction] != null)
            return exits[direction].getExitName();
        else
            return null;
    }

    /**
     * Set the text that should be shown on the UI button for a given direction.
     * @param direction one of the button constants in {@link UIConstants}
     * @param label the text that should be shown on the respective UI button, or null if the button should
     *         be hidden
     */
    public void setExitLabel(int direction, String label) {
        exitLabels[direction] = label;
    }


    /** Returns the text to be displayed when the player enters the room or clicks "Look". */
    public String getDescription() {
        try {
            Meterman2.template.putBinding("room", this);
            if (delegate != null && delegateMethods.contains(GET_DESCRIPTION))
                return delegate.getDescription(this);
            else
                return impl.getDescription(this);
        } finally {
            Meterman2.template.removeBinding("room");
        }
    }

    /**
     * Called when the player has entered the room.
     * @param fromRoom the room (possibly null) from which the player entered
     */
    public void entered(Room fromRoom) {
        if (delegate != null && delegateMethods.contains(ENTERED))
            delegate.entered(this, fromRoom);
        else
            impl.entered(this, fromRoom);
    }

    /**
     * Called as the player is exiting the room (but is still there).
     * @param toRoom the room the player is attempting to move to.
     * @return true to block the player from exiting, false to allow the exit (note that
     *          the exit may fail for other reasons)
     */
    public boolean exiting(Room toRoom) {
        if (delegate != null && delegateMethods.contains(EXITING))
            return delegate.exiting(this, toRoom);
        else
            return impl.exiting(this, toRoom);
    }

    /**
     * Called when a game is saved to get a state object that will be persisted in the saved game file.
     * The object's class should be one of the standard POJO types descripted in {@link Game#getGameStateMap()}
     * @return state object, or null to indicate no state needs to be saved
     */
    public Object getState() {
        if (delegate != null && delegateMethods.contains(GET_STATE))
            return delegate.getState(this);
        else
            return impl.getState(this);
    }

    /**
     * Called when a game is loaded if this room returned non-null state from getState()
     * when the game was saved.
     * @param state non-null state object previously returned by getState()
     */
    public void restoreState(Object state) {
        if (delegate != null && delegateMethods.contains(RESTORE_STATE))
            delegate.restoreState(this, state);
        else
            impl.restoreState(this, state);
    }
}
