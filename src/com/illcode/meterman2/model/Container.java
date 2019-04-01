package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity that can contain other entities (like a chest, shelf, etc.)
 */
public class Container extends Entity implements EntityContainer
{
    private ContainerSupport containerSupport;

    protected Container(String id, EntityImpl impl) {
        super(id, impl);
        containerSupport = new ContainerSupport(this);
    }

    /** Create a container with the given ID and implemention. */
    public static Container create(String id, EntityImpl impl) {
        return new Container(id, impl);
    }

    /** Create a container with the given ID and a container implemention. */
    public static Container create(String id) {
        return create(id, new ContainerImpl());
    }

    //region -- implement EntityContainer
    public int getContainerType() { return CONTAINER_ENTITY; }
    public String getContainerId() { return getId(); }
    public Room getContainerAsRoom() { return null; }
    public Entity getContainerAsEntity() { return this; }
    public Player getContainerAsPlayer() { return null; }
    public void addEntity(Entity e) { containerSupport.addEntity(e); }
    public void clearEntities() { containerSupport.clearEntities(); }
    public List<Entity> getEntities() { return containerSupport.getEntities(); }
    public void removeEntity(Entity e) { containerSupport.removeEntity(e); }
    public boolean containsEntity(Entity e) {return containerSupport.containsEntity(e);}
    //endregion
}
