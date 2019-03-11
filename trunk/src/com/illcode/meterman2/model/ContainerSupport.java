package com.illcode.meterman2.model;

import sun.plugin2.gluegen.runtime.CPU;

import java.util.*;

/**
 * Supports a common implementation of EntityContainer methods.
 */
public final class ContainerSupport implements EntityContainer
{
    private final Entity entity;
    private final Room room;
    private final Player player;
    private final List<Entity> contents;

    /** Construct a container support instance for use with an entity. */
    public ContainerSupport(Entity e) {
        contents = new ArrayList<>();
        this.entity = e;
        this.room = null;
        this.player = null;
    }

    /** Construct a container support instance for use with a room. */
    public ContainerSupport(Room r) {
        contents = new ArrayList<>();
        this.entity = null;
        this.room = r;
        this.player = null;
    }

    /** Construct a container support instance for use with the player. */
    public ContainerSupport(Player p) {
        contents = new ArrayList<>();
        this.room = null;
        this.entity = null;
        this.player = p;
    }

    public int getContainerType() {
        if (entity != null)
            return CONTAINER_ENTITY;
        else if (room != null)
            return CONTAINER_ROOM;
        else
            return CONTAINER_PLAYER;
    }

    public String getContainerId() {
        if (entity != null)
            return entity.getId();
        else if (room != null)
            return room.getId();
        else
            return Player.PLAYER_ID;
    }

    public Room getRoomContainer() {
        return room;
    }

    public Entity getEntityContainer() {
        return entity;
    }

    public Player getPlayerContainer() {
        return player;
    }

    public void addEntity(Entity e) {
        if (!contents.contains(e))
            contents.add(e);
    }

    public void removeEntity(Entity e) {
        contents.remove(e);
    }

    public void clearEntities() {
        contents.clear();
    }

    public List<Entity> getEntities() {
        return contents;
    }
}