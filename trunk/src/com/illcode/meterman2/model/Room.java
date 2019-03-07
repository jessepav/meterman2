package com.illcode.meterman2.model;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.ui.UIConstants;

import java.util.EnumSet;
import java.util.List;

import com.illcode.meterman2.model.RoomImpl.RoomMethod;
import static com.illcode.meterman2.model.RoomImpl.RoomMethod.*;

/**
 * The base class through which the game system and UI interacts with rooms.
 * <p/>
 * Room itself supports method delegation, attributes, and containment; all other implementation is handled
 * by an instance of {@link RoomImpl}. It is that interface, and its base implementation {@link
 * BaseRoomImpl}, that specialized rooms will usually extend.
 */
public class Room implements EntityContainer
{
    protected String id;
    protected RoomImpl impl;

    private RoomImpl delegate;
    private EnumSet<RoomMethod> delegateMethods;
    private AttributeSet attributes;
    private ContainerSupport containerSupport;

    /** Construct a room with the given ID. */
    protected Room(String id, RoomImpl impl) {
        this.id = id;
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
    public AttributeSet getAttributes() {
        return attributes;
    }

    //region -- implement EntityContainer
    public final int getContainerType() { return CONTAINER_ROOM; }
    public final String getContainerId() { return getId(); }
    public final Room getRoomContainer() { return this; }
    public final Entity getEntityContainer() { return null; }
    public final Player getPlayerContainer() { return null; }
    public final void addEntity(Entity e) { containerSupport.addEntity(e); }
    public final void clearEntities() { containerSupport.clearEntities(); }
    public final List<Entity> getEntities() { return containerSupport.getEntities(); }
    public final void removeEntity(Entity e) { containerSupport.removeEntity(e); }
    //endregion

    /** Returns the full name of the room. */
    public String getName() {
        if (delegate != null && delegateMethods.contains(GET_NAME))
            return delegate.getName(this);
        else
            return impl.getName(this);
    }

    /** Returns a potentially shorter version of the name, to be used in Exit buttons.
     *  This method may return a different name depending on whether the room has been visited. */
    public String getExitName() {
        if (delegate != null && delegateMethods.contains(GET_EXIT_NAME))
            return delegate.getExitName(this);
        else
            return impl.getExitName(this);
    }

    /** Returns the text to be displayed when the player enters the room or clicks "Look". */
    public String getDescription() {
        if (delegate != null && delegateMethods.contains(GET_DESCRIPTION))
            return delegate.getDescription(this);
        else
            return impl.getDescription(this);
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
        if (delegate != null && delegateMethods.contains(GET_EXIT))
            return delegate.getExit(this, direction);
        else
            return impl.getExit(this, direction);
    }

    /**
     * Return the text that should be shown on the UI button for a given direction.
     * @param direction one of the button constants in {@link UIConstants}
     * @return the text that should be shown on the respective UI button, or null if the button should
     *         be hidden
     * @see #getExit(int)
     */
    public String getExitLabel(int direction) {
        if (delegate != null && delegateMethods.contains(GET_EXIT_LABEL))
            return delegate.getExitLabel(this, direction);
        else
            return impl.getExitLabel(this, direction);
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
}
