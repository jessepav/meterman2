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

    private Set<Entity> equippedEntities;

    private ContainerSupport containerSupport;

    public Player() {
        containerSupport = new ContainerSupport(this);
        equippedEntities = new HashSet<>();
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
    public Room getContainerAsRoom() { return null; }
    public Entity getContainerAsEntity() { return null; }
    public Player getContainerAsPlayer() { return this; }
    public void addEntity(Entity e) {containerSupport.addEntity(e);}
    public void removeEntity(Entity e) {containerSupport.removeEntity(e);}
    public void clearEntities() {containerSupport.clearEntities();}
    public List<Entity> getEntities() {return containerSupport.getEntities();}
    //endregion

}
