package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemAttributes;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Utility methods that apply to the game world or interface.
 */
public final class GameUtils
{
    /** Convenience method to test if an entity has an attribute. */
    public static boolean hasAttr(Entity e, int attrNum) {
        return e.getAttributes().get(attrNum);
    }

    /** Convenience method to test if a room has an attribute. */
    public static boolean hasAttr(Room r, int attrNum) {
        return r.getAttributes().get(attrNum);
    }

    /** Convenience method to set an entity attribute. */
    public static void setAttr(Entity e, int attrNum) {
        e.getAttributes().set(attrNum);
    }

    /** Convenience method to set a room attribute. */
    public static void setAttr(Room r, int attrNum) {
        r.getAttributes().set(attrNum);
    }

    /**
     * Get the room in which an entity resides, following up the chain of containment if necessary.
     * @param e entity
     * @return the containing room, or null if the entity is not in a room.
     */
    public static Room getRoom(Entity e) {
        return getRoom(e.getContainer());
    }

    /**
     * Get the room in which container resides, following up the chain of containment if necessary.
     * @return the containing room, or null if the container is not in a room.
     */
    public static Room getRoom(EntityContainer container) {
        while (container != null) {
            switch (container.getContainerType()) {
            case EntityContainer.CONTAINER_ROOM:
                return container.getContainerAsRoom();
            case EntityContainer.CONTAINER_PLAYER:
                return Meterman2.gm.getCurrentRoom();
            case EntityContainer.CONTAINER_ENTITY:  // climb up the chain
                container = container.getContainerAsEntity().getContainer();
                break;
            default: // something is wrong
                return null;
            }
        }
        return null;
    }
}
