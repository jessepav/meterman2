package com.illcode.meterman2.model;

import java.util.Collection;
import java.util.List;

/**
 * Implemented by objects that can contain entities. There are three types:
 * <ol>
 *     <li>Rooms, which by nature are containers of entities.</li>
 *     <li>Specialized entities that can hold other entities.</li>
 *     <li>The player.</li>
 * </ol>
 */
public interface EntityContainer
{
    /** This container is another Entity. */
    public static final int CONTAINER_ENTITY = 0;

    /** This container is a Room. */
    public static final int CONTAINER_ROOM = 1;

    /** This container is the player. */
    public static final int CONTAINER_PLAYER = 2;

    /**
     * Return {@code CONTAINER_ENTITY}, {@code CONTAINER_ROOM}, or {@code CONTAINER_PLAYER},
     * as appropriate for the implementing object.
     */
    int getContainerType();

    /** Return the ID of the implementing object. */
    String getContainerId();

    /** If this container is a Room, return it; or null otherwise. */
    Room getContainerAsRoom();

    /** If this container is an Entity, return it; or null otherwise. */
    Entity getContainerAsEntity();

    /** If this container is a Player, return it; or null otherwise. */
    Player getContainerAsPlayer();

    /** Return a list of the entities in this container. */
    List<Entity> getEntities();

    /** Add an entity to this container. */
    void addEntity(Entity e);

    /** Remove an entity from this container.*/
    void removeEntity(Entity e);

    /** Clears (empties) this container. */
    void clearEntities();
}
