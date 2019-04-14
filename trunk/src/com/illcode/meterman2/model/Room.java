package com.illcode.meterman2.model;

import com.illcode.meterman2.*;
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
    public final RoomImpl getImpl() {
        return impl;
    }

    /** Set the room implementation instance used by this Room. */
    public final void setImpl(RoomImpl impl) {
        this.impl = impl;
    }

    /**
     * Set a delegate to which certain methods will be forwarded. This exists primarily to support
     * scripted methods, but may find other uses, such as consolidating logic for multiple rooms in one
     * "manager" class.
     * @param delegate the delegate implementation
     * @param delegateMethods a set indicating which methods should be forwarded
     */
    public final void setDelegate(RoomImpl delegate, EnumSet<RoomMethod> delegateMethods) {
        this.delegate = delegate;
        this.delegateMethods = delegateMethods;
    }

    /** Remove the delegate. */
    public final void clearDelegate() {
        delegate = null;
        delegateMethods = null;
    }

    /** Return the unique ID of this room. */
    public final String getId() {
        return id;
    }

    /** Return this room's attributes, a mutable set that the caller can query and manipulate. */
    public final AttributeSet getAttributes() {
        return attributes;
    }

    /** Returns the name of the room. */
    public String getName() {
        return name != null ? name : "[" + getId() + "]";
    }

    /** Returns the name property of the room. Intended for internal use. */
    public final String getNameProperty() {
        return name;
    }

    /** Set the name of the room. */
    public final void setName(String name) {
        this.name = name;
    }

    /** Returns a potentially shorter version of the name, to be used in Exit buttons. */
    public String getExitName() {
        return exitName != null ? exitName : getName();
    }

    /** Returns the exit name property of the room. Intended for internal use. */
    public final String getExitNameProperty() {
        return exitName;
    }

    /** Set the version of the name to be used in Exit buttons. */
    public final void setExitName(String exitName) {
        this.exitName = exitName;
    }

    /**
     * Return the room associated with a given exit position. This method is used in the world model
     * to get the actual room to which an exit leads.
     * @param position one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @return the room that is found when exiting this room in the given position, or null if no
     *         exit is possible in that position.
     * @see #getExitLabel(int)
     */
    public Room getExit(int position) {
        return exits[position];
    }

    /** Returns the exit property of the room for the given position. Intended for internal use. */
    public final Room getExitProperty(int position) {
        return exits[position];
    }

    /**
     * Set the room associated with a given exit position.
     * @param position one of the button constants in {@link UIConstants}, ex {@link UIConstants#NW_BUTTON}
     * @param destination the room that is found when exiting this room in the given position, or null if no
     *         exit is possible in that position.
     */
    public final void setExit(int position, Room destination) {
        exits[position] = destination;
    }

    /**
     * Return the text that should be shown on the UI button for a given position.
     * @param position one of the button constants in {@link UIConstants}
     * @return the text that should be shown on the respective UI button, or null if the button should
     *         be hidden
     */
    public String getExitLabel(int position) {
        if (exitLabels[position] != null)
            return exitLabels[position];
        else if (exits[position] != null)
            return exits[position].getExitName();
        else
            return null;
    }

    /** Returns the exit label property for a given position. Intended for internal use. */
    public final String getExitLabelProperty(int position) {
        return exitLabels[position];
    }

    /**
     * Set the text that should be shown on the UI button for a given position.
     * @param position one of the button constants in {@link UIConstants}
     * @param label the text that should be shown on the respective UI button, or null if the exit name
     * of the room in the given position should be used, if any.
     */
    public final void setExitLabel(int position, String label) {
        exitLabels[position] = label;
    }


    /** Returns the text to be displayed when the player enters the room or clicks "Look". */
    public String getDescription() {
        try {
            GameUtils.pushBinding("room", this);
            if (delegate != null && delegateMethods.contains(GET_DESCRIPTION))
                return delegate.getDescription(this);
            else
                return impl.getDescription(this);
        } finally {
            GameUtils.popBinding("room");
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
     * Called at the end of each turn that the player is in this room.
     */
    public void eachTurn() {
        if (delegate != null && delegateMethods.contains(EACH_TURN))
            delegate.eachTurn(this);
        else
            impl.eachTurn(this);
    }

    /**
     * Called when a game is saved to get a state object that will be persisted in the saved game file.
     * The object's class should be one of the standard POJO types descripted in {@link Game#getInitialGameStateMap()}
     * @return state object, or null to indicate no state needs to be saved
     */
    public Object getState() {
        if (delegate != null && delegateMethods.contains(GET_STATE))
            return delegate.getState(this);
        else
            return impl.getState(this);
    }

    /**
     * Called when a game is loaded to restore room state. It is always called as the
     * final step of the process of patching up the room's state.
     * @param state state object (possibly null) previously returned by getState().
     */
    public void restoreState(Object state) {
        if (delegate != null && delegateMethods.contains(RESTORE_STATE))
            delegate.restoreState(this, state);
        else
            impl.restoreState(this, state);
    }

    /**
     * Called immediately before {@link Game#start} for both new and loaded games.
     * This is a good place to do things like register oneself as a game-event handler
     * with the game manager.
     */
    public void gameStarting() {
        if (delegate != null && delegateMethods.contains(GAME_STARTING))
            delegate.gameStarting(this);
        else
            impl.gameStarting(this);
    }

    /**
     * Return the name of the room prefixed by the definite article ("the"), taking into
     * account proper names.
     * @param capitalize whether to capitalize the definite article
     */
    public final String getDefName(boolean capitalize) {
        final String name = getName();
        if (name.isEmpty())
            return "";
        if (getAttributes().get(SystemAttributes.PROPER_NAME))
            return name;
        final String defArt = capitalize ? "The " : "the ";
        return defArt + name;
    }

    /**
     * Return the name of the room prefixed by the definite article ("the") in lowercase,
     * taking into account proper names.
     */
    public final String getDefName() {
        return getDefName(false);
    }

    //region -- implement EntityContainer
    public int getContainerType() { return CONTAINER_ROOM; }
    public String getContainerId() { return getId(); }
    public Room getContainerAsRoom() { return this; }
    public Entity getContainerAsEntity() { return null; }
    public Player getContainerAsPlayer() { return null; }
    public void addEntity(Entity e) { containerSupport.addEntity(e); }
    public void clearEntities() { containerSupport.clearEntities(); }
    public List<Entity> getEntities() { return containerSupport.getEntities(); }
    public void removeEntity(Entity e) { containerSupport.removeEntity(e); }
    public boolean containsEntity(Entity e) {return containerSupport.containsEntity(e);}
    //endregion

    @Override
    public String toString() {
        return getName();
    }
}
