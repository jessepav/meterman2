package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    /**
     * Checks if a given container is a parent container of a given entity.
     * @param target the container to test for parenthood
     * @param e entity
     * @return true if <em>target</em> is a parent container of <em>e</em>
     */
    public static boolean isParentContainer(EntityContainer target, Entity e) {
        return isParentContainer(target, e.getContainer());
    }

    /**
     * Checks if a given container is a parent of another container.
     * @param target the container to test for parenthood
     * @param c container
     * @return true if <em>target</em> is a parent container of <em>c</em>
     */
    public static boolean isParentContainer(EntityContainer target, EntityContainer c) {
        while (c != null) {
            if (c == target)
                return true;
            else if (c == Meterman2.gm.getPlayer())  // only the room above the player remains
                return target == Meterman2.gm.getCurrentRoom();
            else if (c.getContainerType() == EntityContainer.CONTAINER_ENTITY)
                c = c.getContainerAsEntity().getContainer();
            else
                break;
        }
        return false;
    }

    /**
     * Return a list of all the entities in a given container, and if any of those entities is itself
     * a container, its contents recursively.
     */
    public static List<Entity> getEntitiesRecursive(EntityContainer container) {
        List<Entity> l = new ArrayList<>();
        getEntitiesRecursiveHelper(container, l);
        return l;
    }

    private static void getEntitiesRecursiveHelper(EntityContainer c, List<Entity> l) {
        for (Entity e : c.getEntities()) {
            l.add(e);
            if (e instanceof EntityContainer)
                getEntitiesRecursiveHelper((EntityContainer) e, l);
        }
    }
}
