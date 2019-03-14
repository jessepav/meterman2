package com.illcode.meterman2.model;

import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;

/**
 * Used by loaders to resolve references among game objects.
 */
public interface GameObjectIdResolver
{
    /** Return the entity with the given ID, or null if not found. */
    Entity getEntity(String id);

    /** Return the room with the given ID, or null if not found. */
    Room getRoom(String id);
}
