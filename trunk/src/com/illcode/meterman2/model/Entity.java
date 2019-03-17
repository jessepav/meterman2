package com.illcode.meterman2.model;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.GameManager;
import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemAttributes;
import com.illcode.meterman2.model.EntityImpl.EntityMethod;
import org.apache.commons.lang3.text.WordUtils;

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

    /**
     * Return the entity description (never null).
     * <p/>
     * {@code TextSource}S involved in the generation of the description will have "entity" bound
     * to this entity in the script and template namespaces.
     */
    public String getDescription() {
        try {
            Meterman2.script.putBinding("entity", this);
            Meterman2.template.putBinding("entity", this);
            if (delegate != null && delegateMethods.contains(GET_DESCRIPTION))
                return delegate.getDescription(this);
            else
                return impl.getDescription(this);
        } finally {
            Meterman2.script.removeBinding("entity");
            Meterman2.template.removeBinding("entity");
        }
    }

    /**
     * Called on each entity in a room when a look command is issued. While the implementation is free to do
     * anything, generally it will call {@link GameManager#queueLookText(String, boolean)} to add text to
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
     *     <li>The entity is added to a container in the current room,
     *         from somewhere outside the current room</li>
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
     *     <li>The entity is being moved from somewhere in the current room to somewhere outside it</li>
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
     * The player invoked an action on this entity from the UI.
     * <p/>
     * If there is an entity delegate registered for this method, and that delegate returns
     * false, then we continue on to our normal implementation. This allows delegates to handle
     * only specific actions, while leaving the rest to the normal implementation.
     * <p/>
     * {@code TextSource}S invoked during action processing will have "entity" bound
     * to this entity in the script and template namespaces.
     * @param action action name
     * @return true if the entity processed the action itself, false to continue
     *              through the processing chain
     */
    public boolean processAction(Action action) {
        try {
            Meterman2.script.putBinding("entity", this);
            Meterman2.template.putBinding("entity", this);
            if (delegate != null && delegateMethods.contains(PROCESS_ACTION))
                if(delegate.processAction(this, action))
                    return true;
            return impl.processAction(this, action);
        } finally {
            Meterman2.script.removeBinding("entity");
            Meterman2.template.removeBinding("entity");
        }
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
     * Called when a game is loaded to restore entity state.
     * @param state state object (possibly null) previously returned by getState().
     */
    public void restoreState(Object state) {
        if (delegate != null && delegateMethods.contains(RESTORE_STATE))
            delegate.restoreState(this, state);
        else
            impl.restoreState(this, state);
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the"), taking into
     * account proper names.
     * @param capitalize whether to capitalize the definite article
     */
    public String getDefName(boolean capitalize) {
        String name = getName();
        if (name.isEmpty())
            return "";
        if (getAttributes().get(SystemAttributes.PROPER_NAME))
            return name;
        String defArt = capitalize ? "The " : "the ";
        return defArt + name;
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the") in lowercase,
     * taking into account proper names.
     */
    public String getDefName() {
        return getDefName(false);
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other"), taking into account
     * proper names. If {@code e.getIndefiniteArticle()} returns null, we use "an" for names that begin with
     * a vowel, and "a" otherwise.
     * @param capitalize whether to capitalize the indefinite article
     */
    public String getIndefName(boolean capitalize) {
        String name = getName();
        if (name.isEmpty())
            return "";
        if (getAttributes().get(SystemAttributes.PROPER_NAME))
            return name;
        String indefArt = getIndefiniteArticle();
        if (indefArt == null) {
            char c = Character.toLowerCase(name.charAt(0));
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u')
                indefArt = "an";
            else
                indefArt = "a";
        }
        if (capitalize)
            indefArt = WordUtils.capitalize(indefArt);
        return indefArt + " " + name;
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other") in lowercase.
     * @param e entity
     */
    public String getIndefName() {
        return getIndefName(false);
    }

}
