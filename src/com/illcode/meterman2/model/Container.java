package com.illcode.meterman2.model;

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
