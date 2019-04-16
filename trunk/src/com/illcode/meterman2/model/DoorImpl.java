package com.illcode.meterman2.model;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.ui.UIConstants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.illcode.meterman2.GameUtils.getPassageWithArgs;
import static com.illcode.meterman2.GameUtils.printPassageWithArgs;
import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.SystemAttributes.CLOSED;
import static com.illcode.meterman2.SystemAttributes.LOCKED;

/**
 * Implementation of door entities, is a special usage entity that can connect and disconnect two rooms,
 * depending on whether it is open.
 */
public class DoorImpl extends BaseEntityImpl
{
    protected Room room1, room2;
    protected int pos1, pos2;
    protected String closedExitLabel;
    protected Entity key;

    /**
     * Create a new door implementation.
     */
    public DoorImpl() {
        super();
    }

    /** Set the two rooms connected by this door. */
    public void setRooms(Room room1, Room room2) {
        this.room1 = room1;
        this.room2 = room2;
    }

    /** Get the two rooms connected by this door. */
    public Pair<Room,Room> getRooms() {
        return Pair.of(room1, room2);
    }

    /**
     * Return the other side of the door connection for a given room.
     * @param thisRoom the room whose other side to get
     * @return other room, or null if <em>thisRoom</em> is not one of the rooms connected by the door.
     */
    public Room getOtherRoom(Room thisRoom) {
        if (thisRoom == room1)
            return room2;
        else if (thisRoom == room2)
            return room1;
        else
            return null;
    }

    /**
     * Sets the positions (ex. {@link UIConstants#NW_BUTTON}) in the first and
     * second room to be connected when the door is unlocked.
     * @param pos1 exit position in first room
     * @param pos2 exit position in second room
     */
    public void setPositions(int pos1, int pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Sets the label that will be shown on the room exits when the door is closed.
     * @param label exit label, or null
     */
    public void setClosedExitLabel(String label) {
        this.closedExitLabel = label;
    }

    /**
     * Get the key needed to lock and unlock this door.
     * @return the key entity, or null if no key is required
     */
    public Entity getKey() {
        return key;
    }

    /**
     * Sets the key needed to lock and unlock this door. The key is an Entity
     * that must be in the player's inventory to perform these operations.
     * @param key key entity, or null if no key is required
     */
    public void setKey(Entity key) {
        this.key = key;
    }

    @Override
    public String getDescription(Entity e) {
        String description = super.getDescription(e);
        if (e.getAttributes().get(LOCKED))
            return description + " " + getPassageWithArgs("locked-message", e.getDefName());
        else
            return description;
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        AttributeSet attr = e.getAttributes();
        List<MMActions.Action> actions = super.getActions(e);
        if (attr.get(LOCKED)) {
            if (key != null)
                actions.add(SystemActions.UNLOCK);
        } else { // okay, we're unlocked
            if (attr.get(CLOSED)) { // closed but unlocked
                actions.add(SystemActions.OPEN);
                if (key != null)
                    actions.add(SystemActions.LOCK);
            } else { // we're open
                actions.add(SystemActions.CLOSE);
            }
        }
        return actions;
    }

    @Override
    public boolean processAction(Entity e, MMActions.Action action) {
        Room currentRoom = gm.getCurrentRoom();
        if (currentRoom != room1 && currentRoom != room2)
            return false;
        AttributeSet attr = e.getAttributes();
        if (action.equals(SystemActions.LOCK) || action.equals(SystemActions.UNLOCK)) {
            // key should not be null (LOCK and UNLOCK shouldn't have been added),
            // but if it is, we act as though you don't have the key
            if (key == null || !gm.isInInventory(key)) {
                printPassageWithArgs("nokey-message", e.getDefName());
            } else {
                attr.toggle(LOCKED);
                final String message = attr.get(LOCKED) ? "lock-message" : "unlock-message";
                printPassageWithArgs(message, e.getDefName(), key.getDefName());
                gm.entityChanged(e);
            }
            return true;
        } else if (action.equals(SystemActions.OPEN) || action.equals(SystemActions.CLOSE)) {
            attr.toggle(CLOSED);
            final String message = attr.get(CLOSED) ? "close-message" : "open-message";
            printPassageWithArgs(message, e.getDefName());
            updateRoomConnections(e);
            gm.entityChanged(e);
            gm.roomChanged(room1);
            gm.roomChanged(room2);
            return true;
        }
        return false;
    }

    @Override
    public void restoreState(Entity e, Object state) {
        super.restoreState(e, state);
        e.setContainer(null);  // doors are in two places at once.
    }

    /**
     * Connects or disconnects the door's two rooms depending on if it's open or closed.
     */
    public void updateRoomConnections(Entity e) {
        if (e.getAttributes().get(CLOSED)) {
            room1.setExit(pos1, null);
            room1.setExitLabel(pos1, closedExitLabel);
            room2.setExit(pos2, null);
            room2.setExitLabel(pos2, closedExitLabel);
        } else {
            room1.setExit(pos1, room2);
            room1.setExitLabel(pos1, null);  // use room2's exit label
            room2.setExit(pos2, room1);
            room2.setExitLabel(pos2, null);  // use room1's exit label
        }
    }
}
