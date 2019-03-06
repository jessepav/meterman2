package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.AttributeSet;
import static com.illcode.meterman2.model.EntityImplMethods.*;

import java.util.EnumSet;
import java.util.List;

/**
 * The base class through which the game system and UI interacts with entities.
 * <p/>
 * Entity itself supports method delegation, attributes, and containment; all other implementation is
 * handled by an instance of {@link EntityImpl}. It is that interface, and its base implementation {@link
 * BaseEntityImpl}, that specialized entities will usually extend.
 */
public class Entity
{
    protected String id;
    protected EntityImpl impl;

    private EntityImpl delegate;
    private EnumSet<EntityImplMethods> delegateMethods;
    private AttributeSet attributes;
    private EntityContainer container;

    protected Entity(String id, EntityImpl impl) {
        this.id = id;
        this.impl = impl;
        attributes = new AttributeSet();
    }

    /** Create an entity with the given ID and a basic implemention. */
    public static Entity create(String id) {
        return create(id, new BaseEntityImpl());
    }

    /** Create an entity with the given ID and implemention. */
    public static Entity create(String id, EntityImpl impl) {
        return new Entity(id, impl);
    }

    /** Return the entity implementation instance used by this Entity. */
    public EntityImpl getImpl() {
        return impl;
    }

    /** Set the entity implementation instance used by this Entity. */
    public void setImpl(EntityImpl impl) {
        this.impl = impl;
    }

    /**
     * Set a delegate to which certain methods will be forwarded. This exists primarily to support
     * scripted methods, but may find other uses, such as consolidating logic for multiple entities in one
     * "manager" class.
     * @param delegate the delegate implementation
     * @param delegateMethods a set indicating which methods should be forwarded
     */
    public void setDelegate(EntityImpl delegate, EnumSet<EntityImplMethods> delegateMethods) {
        this.delegate = delegate;
        this.delegateMethods = delegateMethods;
    }

    /** Remove the delegate. */
    public void clearDelegate() {
        delegate = null;
        delegateMethods = null;
    }

    /** Return the unique ID of this entity. */
    public String getId() {
        return id;
    }

    /** Return this entity's attributes, a mutable set that the caller can query and manipulate. */
    public AttributeSet getAttributes() {
        return attributes;
    }

    /** Return the container that holds this entity, or null if none. */
    public EntityContainer getContainer() {
        return container;
    }

    /**
     * Set the container that holds this entity.
     * @param container the entity container that holds this entity, or null to
     *         indicate the entity is removed from the game world.
     */
    public void setContainer(EntityContainer container) {
        this.container = container;
    }

    /** Return the name of this entity */
    public String getName() {
        if (delegate != null && delegateMethods.contains(GET_NAME))
            return delegate.getName(this);
        else
            return impl.getName(this);
    }

    /** Return the entity description. */
    public String getDescription() {
        if (delegate != null && delegateMethods.contains(GET_DESCRIPTION))
            return delegate.getDescription(this);
        else
            return impl.getDescription(this);
    }

    /**
     * Called on each entity in a room when a look command is issued. While the implementation is free to do
     * anything, generally it will call {@code GameManager.queueLookText()} to add text to
     * the room description printed.
     */
    public void lookInRoom() {
        if (delegate != null && delegateMethods.contains(LOOK_IN_ROOM))
            delegate.lookInRoom(this);
        else
            impl.lookInRoom(this);
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
        if (delegate != null && delegateMethods.contains(ENTER_SCOPE))
            delegate.enterScope(this);
        else
            impl.enterScope(this);
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
        if (delegate != null && delegateMethods.contains(EXITING_SCOPE))
            delegate.exitingScope(this);
        else
            impl.exitingScope(this);
    }

    /** Called when the entity is moved to the player inventory */
    public void taken() {
        if (delegate != null && delegateMethods.contains(TAKEN))
            delegate.taken(this);
        else
            impl.taken(this);
    }

    /** Called when the entity is removed from the player inventory */
    public void dropped() {
        if (delegate != null && delegateMethods.contains(DROPPED))
            delegate.dropped(this);
        else
            impl.dropped(this);
    }

    /**
     * Returns the room  where this entity is found. If the entity is held in player inventory,
     * this returns the current player room.
     * @return the room, or null if not in a room or inventory
     */
    public Room getRoom() {
        if (delegate != null && delegateMethods.contains(GET_ROOM))
            return delegate.getRoom(this);
        else
            return impl.getRoom(this);

    }

    /**
     * Sets the room where this entity resides, or null if it doesn't reside anywhere.
     * @param room
     */
    public void setRoom(Room room) {
        if (delegate != null && delegateMethods.contains(SET_ROOM))
            delegate.setRoom(this, room);
        else
            impl.setRoom(this, room);
    }

    /**
     * Returns a list of extra actions to be shown in the UI. Never returns null.
     */
    List<String> getActions() {
        if (delegate != null && delegateMethods.contains(GET_ACTIONS))
            return delegate.getActions(this);
        else
            return impl.getActions(this);

    }

    /**
     * The player invoked an action on this entity from the UI
     * @param action action name
     * @return true if the entity processed the action itself, false to continue
     *              through the processing chain
     */
    boolean processAction(MMActions.Action action) {
        if (delegate != null && delegateMethods.contains(PROCESS_ACTIONS))
            return delegate.processAction(this, action);
        else
            return impl.processAction(this, action);
    }
}
