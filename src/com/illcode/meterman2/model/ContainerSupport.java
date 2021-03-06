package com.illcode.meterman2.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Supports a common implementation of EntityContainer methods.
 */
public final class ContainerSupport implements EntityContainer
{
    private final Entity entity;
    private final Room room;
    private final Player player;
    private final List<Entity> contents;
    private final Set<Entity> contentsSet;

    /** Construct a container support instance for use with an entity. */
    public ContainerSupport(Entity e) {
        contents = new ArrayList<>();
        contentsSet = new HashSet<>();
        this.entity = e;
        this.room = null;
        this.player = null;
    }

    /** Construct a container support instance for use with a room. */
    public ContainerSupport(Room r) {
        contents = new ArrayList<>();
        contentsSet = new HashSet<>();
        this.entity = null;
        this.room = r;
        this.player = null;
    }

    /** Construct a container support instance for use with the player. */
    public ContainerSupport(Player p) {
        contents = new ArrayList<>();
        contentsSet = new HashSet<>();
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

    public Room getContainerAsRoom() {
        return room;
    }

    public Entity getContainerAsEntity() {
        return entity;
    }

    public Player getContainerAsPlayer() {
        return player;
    }

    public void addEntity(Entity e) {
        if (e != null && contentsSet.add(e))
            contents.add(e);
    }

    public void removeEntity(Entity e) {
        if (contentsSet.remove(e))
            contents.remove(e);
    }

    public boolean containsEntity(Entity e) {
        return contentsSet.contains(e);
    }

    public void clearEntities() {
        contents.clear();
        contentsSet.clear();
    }

    public List<Entity> getEntities() {
        return contents;
    }
}
