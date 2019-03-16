package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity that can contain other entities (like a chest, shelf, etc.)
 */
public class Container extends Entity implements EntityContainer
{
    protected String inPrep;
    protected String outPrep;
    protected Entity key;

    private ContainerSupport containerSupport;

    protected Container(String id, EntityImpl impl) {
        super(id, impl);
        containerSupport = new ContainerSupport(this);
    }

    /** Create a container with the given ID and a container implemention. */
    public static Container create(String id) {
        return create(id, new ContainerImpl());
    }

    /** Create a container with the given ID and implemention. */
    public static Container create(String id, EntityImpl impl) {
        return new Container(id, impl);
    }

    /**
     * Return the preposition used when putting an object into the container
     * (ex. "in" for a box, and "on" for a shelf).
     */
    public String getInPrep() {
        return inPrep != null ? inPrep : "in";
    }

    /** Set the preposition used when putting an object into the container. */
    public void setInPrep(String inPrep) {
        this.inPrep = inPrep;
    }

    /**
     * Return the preposition used when taking an object out of the container
     * (ex. "from" for a box, and "off" for a shelf).
     */
    public String getOutPrep() {
        return outPrep != null ? outPrep : "from";
    }

    /** Set the preposition used when taking an object out of the container. */
    public void setOutPrep(String outPrep) {
        this.outPrep = outPrep;
    }

    /** Get the key used to lock/unlock this container. Null means no key is needed. */
    public Entity getKey() {
        return key;
    }

    /** Set the key used to lock/unlock this container. Null means no key is needed. */
    public void setKey(Entity key) {
        this.key = key;
    }

    //region -- implement EntityContainer
    public final int getContainerType() { return CONTAINER_ENTITY; }
    public final String getContainerId() { return getId(); }
    public final Room getContainerAsRoom() { return null; }
    public final Entity getContainerAsEntity() { return this; }
    public final Player getContainerAsPlayer() { return null; }
    public final void addEntity(Entity e) { containerSupport.addEntity(e); }
    public final void clearEntities() { containerSupport.clearEntities(); }
    public final List<Entity> getEntities() { return containerSupport.getEntities(); }
    public final void removeEntity(Entity e) { containerSupport.removeEntity(e); }
    //endregion
}
