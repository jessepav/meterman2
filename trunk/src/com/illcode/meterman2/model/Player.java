package com.illcode.meterman2.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The player character.
 */
public final class Player implements EntityContainer
{
    /** A special, unique object ID (cf. entity and room IDs) representing the player character. */
    public static final String PLAYER_ID = "__PLAYER_ID__";

    private EntityContainer container;
    private ContainerSupport containerSupport;

    private Set<Entity> equippedEntities;

    public Player() {
        containerSupport = new ContainerSupport(this);
        equippedEntities = new HashSet<>();
    }

    /** Return the container where the player resides. */
    public EntityContainer getContainer() {
        return container;
    }

    /** Set the container where the player resides. */
    public void setContainer(EntityContainer container) {
        this.container = container;
    }

    public void equipEntity(Entity e) {
        if (getEntities().contains(e))
            equippedEntities.add(e);
    }

    public void unequipEntity(Entity e) {
        equippedEntities.remove(e);
    }

    public Set<Entity> getEquippedEntities() {
        return equippedEntities;
    }

    public void clearEquippedEntities() {
        equippedEntities.clear();
    }

    //region -- implement EntityContainer
    public int getContainerType() { return CONTAINER_PLAYER; }
    public String getContainerId() { return PLAYER_ID; }
    public Room getRoomContainer() { return null; }
    public Entity getEntityContainer() { return null; }
    public Player getPlayerContainer() { return this; }
    public void addEntity(Entity e) {containerSupport.addEntity(e);}
    public void removeEntity(Entity e) {containerSupport.removeEntity(e);}
    public void clearEntities() {containerSupport.clearEntities();}
    public List<Entity> getEntities() {return containerSupport.getEntities();}
    //endregion

}
