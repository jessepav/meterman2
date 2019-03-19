package com.illcode.meterman2;

import com.illcode.meterman2.model.DoorImpl;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.EntityImpl;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.ui.UIConstants;

import static com.illcode.meterman2.SystemAttributes.CLOSED;
import static com.illcode.meterman2.SystemAttributes.LOCKED;

import java.util.*;

/**
 * Finds the shortest path between two rooms, if one exists, taking into account unlocked doors.
 */
public class Pathfinder
{
    private Queue<PathNode> openQueue;
    private HashSet<PathNode> closedSet;

    public Pathfinder() {
        openQueue = new LinkedList<>();
        closedSet = new HashSet<>();
    }

    /**
     * Attempts to find a path between two rooms.
     * <p/>
     * Since in our world model it is rather impossible to make estimations as to the "cost" between
     * two rooms, we just do a simple breadth-first-search (BFS).
     * @param start room from which to start pathfinding
     * @param destination room to which we're attempting to find a path
     * @return a list of rooms that is the path from start to goal, not including
     *          the start room, or null if no path found.
     */
    public List<Room> findPath(Room start, Room destination) {
        openQueue.add(new PathNode(start, null));
        Set<PathNode> neighbors = new HashSet<>();
        try {
            while(!openQueue.isEmpty()) {
                PathNode currentNode = openQueue.remove();
                if (currentNode.room == destination)
                    return constructPath(currentNode);
                closedSet.add(currentNode);
                neighbors.clear();
                gatherNeighbors(currentNode, neighbors);
                for (PathNode neighbor : neighbors) {
                    if (!openQueue.contains(neighbor) && !closedSet.contains(neighbor))
                        openQueue.add(neighbor);
                }
            }
            return null;  // no path found
        } finally {
            openQueue.clear();
            closedSet.clear();
        }
    }

    private void gatherNeighbors(PathNode currentNode, Set<PathNode> neighbors) {
        // First add all the normal exit neighbors of the room
        Room currentRoom = currentNode.room;
        for (int direction = 0; direction < UIConstants.NUM_EXIT_BUTTONS; direction++) {
            Room r = currentRoom.getExit(direction);
            if (r != null)
                neighbors.add(new PathNode(r, currentNode));
        }
        // Then check for closed, unlocked doors.
        for (Entity e : currentRoom.getEntities()) {
            EntityImpl impl = e.getImpl();
            if (impl instanceof DoorImpl) {
                DoorImpl d = (DoorImpl) impl;
                AttributeSet attr = e.getAttributes();
                if (attr.get(CLOSED) && !attr.get(LOCKED)) {
                    Room otherRoom = d.getOtherRoom(currentRoom);
                    if (otherRoom != null)
                        neighbors.add(new PathNode(otherRoom, currentNode));
                }
            }
        }
    }

    private List<Room> constructPath(PathNode node) {
        LinkedList<Room> path = new LinkedList<>();
        while (node.parent != null) {
            path.addFirst(node.room);
            node = node.parent;
        }
        return path;
    }

    private static class PathNode
    {
        Room room;
        PathNode parent;

        private PathNode(Room room, PathNode parent) {
            this.room = room;
            this.parent = parent;
        }

        public boolean equals(Object obj) {
            return room.equals(obj);
        }

        public int hashCode() {
            return room.hashCode();
        }
    }
}
