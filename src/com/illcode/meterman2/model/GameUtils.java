package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemAttributes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Utility methods that apply to the game world or interface.
 */
public final class GameUtils
{
    /** Convenience method to test if an entity has an attribute. */
    public static boolean hasAttr(Entity e, int attrNum) {
        return e.getAttributes().get(attrNum);
    }

    /** Convenience method to set an entity attribute. */
    public static void setAttr(Entity e, int attrNum) {
        e.getAttributes().set(attrNum);
    }

    /** Convenience method to set an entity attribute. */
    public static void setAttr(Entity e, int attrNum, boolean value) {
        e.getAttributes().set(attrNum, value);
    }

    /** Convenience method to test if a room has an attribute. */
    public static boolean hasAttr(Room r, int attrNum) {
        return r.getAttributes().get(attrNum);
    }

    /** Convenience method to set a room attribute. */
    public static void setAttr(Room r, int attrNum) {
        r.getAttributes().set(attrNum);
    }

    /** Convenience method to set a room attribute. */
    public static void setAttr(Room r, int attrNum, boolean value) {
        r.getAttributes().set(attrNum, value);
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
        // I'm going to write this iteratively instead of recursively, just because.
        List<Entity> entities = new ArrayList<>();
        Queue<EntityContainer> pendingContainers = new LinkedList<>();
        pendingContainers.add(container);
        while (!pendingContainers.isEmpty()) {
            EntityContainer c = pendingContainers.remove();
            for (Entity e : c.getEntities()) {
                entities.add(e);
                if (e instanceof EntityContainer)
                    pendingContainers.add((EntityContainer) e);
            }
        }
        return entities;
    }

    /**
     * Returns the subset of a given list of entities that have a specific attribute value.
     * @param entities list of entities to filter
     * @param attrNum attribute to filter by
     * @param value the value of the attribute
     * @return a new list of entities that have {@code attribute = value}
     */
    public static List<Entity> filterByAttribute(List<Entity> entities, int attrNum, boolean value) {
        List<Entity> filteredList = new LinkedList<>();
        filterByAttribute(entities, attrNum, value, filteredList);
        return filteredList;
    }

    /**
     * Adds the subset of a given list of entities that have a specific attribute value to a target list.
     * @param entities list of entities to filter
     * @param attrNum attribute number to filter by
     * @param value the value of the attribute
     * @param target the list to which filtered entities will be added
     */
    public static void filterByAttribute(List<Entity> entities, int attrNum, boolean value, List<Entity> target) {
        for (Entity e : entities)
            if (e.getAttributes().get(attrNum) == value)
                target.add(e);
    }

    /**
     * Returns a list of all the {@link SystemAttributes#TAKEABLE} entities in the current room and the player
     * inventory. This is useful in situations, for instance, where some object has a slot that the
     * player can put something into. The object has an action "Put in Slot" that, when activated, will
     * prompt the player to choose what to put it -- and presumably only things that are <tt>TAKEABLE</tt> can
     * be lifted and put in.
     */
    public static List<Entity> getCurrentTakeableEntities() {
        List<Entity> takeables = new LinkedList<>();
        getCurrentTakeableEntities(takeables);
        return takeables;
    }

    /**
     * Like {@link #getCurrentTakeableEntities()}, but uses a list given as a parameter to avoid allocation.
     */
    public static void getCurrentTakeableEntities(List<Entity> takeables) {
        filterByAttribute(Meterman2.gm.getCurrentRoom().getEntities(), SystemAttributes.TAKEABLE, true, takeables);
        filterByAttribute(Meterman2.gm.getPlayer().getEntities(), SystemAttributes.TAKEABLE, true, takeables);
    }
}
