package com.illcode.meterman2.model;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.model.EntityImpl.EntityMethod;

import java.util.EnumSet;
import java.util.List;

import static com.illcode.meterman2.model.EntityImpl.EntityMethod.*;

/**
 * The base class through which the game system and UI interacts with entities.
 * <p/>
 * Entity itself handles its name, method delegation, attributes, and containment; all other implementation
 * is handled by an instance of {@link EntityImpl}. It is that interface, and its base implementation {@link
 * BaseEntityImpl}, that specialized entities will usually extend.
 */
public class Entity
{
    // These comprise the standard properties of an entity, and will be persisted.
    protected String id;
    protected String name;
    protected String indefiniteArticle;
    private AttributeSet attributes;
    private EntityContainer container;

    // These are behavioral, and are not persisted.
    protected EntityImpl impl;
    private EntityImpl delegate;
    private EnumSet<EntityMethod> delegateMethods;

    protected Entity(String id, EntityImpl impl) {
        this.id = id;
        this.impl = impl;
        attributes = AttributeSet.create();
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
    public void setDelegate(EntityImpl delegate, EnumSet<EntityMethod> delegateMethods) {
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
    public final AttributeSet getAttributes() {
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

    /** Return the name of this entity (never null). */
    public String getName() {
        return name != null ? name : "[" + getId() + "]";
    }

    /** Set the name of this entity. */
    public void setName(String name) {
        this.name = name;
    }

    /** Return the indefinite article used when referring to this entity (may be null). */
    public String getIndefiniteArticle() {
        return indefiniteArticle;
    }

    /** Set the indefinite article used when referring to this entity. */
    public void setIndefiniteArticle(String indefiniteArticle) {
        this.indefiniteArticle = indefiniteArticle;
    }

    /** Return the entity description (never null). */
    public String getDescription() {
        try {
            Meterman2.template.putBinding("entity", this);
            if (delegate != null && delegateMethods.contains(GET_DESCRIPTION))
                return delegate.getDescription(this);
            else
                return impl.getDescription(this);
        } finally {
            Meterman2.template.removeBinding("entity");
        }
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
     * Returns a list of extra actions to be shown in the UI. Never returns null.
     */
    public List<Action> getActions() {
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
    public boolean processAction(Action action) {
        if (delegate != null && delegateMethods.contains(PROCESS_ACTION))
            return delegate.processAction(this, action);
        else
            return impl.processAction(this, action);
    }
}
