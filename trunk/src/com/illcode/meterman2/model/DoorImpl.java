package com.illcode.meterman2.model;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.text.TextSource;

import java.util.ArrayList;
import java.util.List;

import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.SystemAttributes.*;
import static com.illcode.meterman2.Meterman2.gm;

/**
 * Implementation of door entities, is a special usage entity that can connect and disconnect two rooms,
 * depending on whether it is open.
 */
public class DoorImpl extends BaseEntityImpl
{
    public static final String LOCKED_MESSAGE_PASSAGE_ID = "door-locked-message";
    public static final String NOKEY_MESSAGE_PASSAGE_ID = "door-nokey-message";

    protected Room room1, room2;
    protected int pos1, pos2;
    protected String closedExitLabel;
    protected TextSource lockedMessage;
    protected TextSource noKeyMessage;
    protected Entity key;

    protected List<MMActions.Action> actions;

    /**
     * Create a new door implementation. You must call {@link #setEntity(Entity)} to attach
     * this implementation to an entity before it will be functional.
     */
    public DoorImpl() {
        super();
        actions = new ArrayList<>(4);
    }

    /** Set the two rooms connected by this door. */
    public void setRooms(Room room1, Room room2) {
        this.room1 = room1;
        this.room2 = room2;
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
     * @param label exit label
     */
    public void setClosedExitLabel(String label) {
        this.closedExitLabel = label;
    }

    private String getLockedMessage(Entity e) {
        return lockedMessage != null
            ? lockedMessage.getText(e.defName())
            : bundles.getPassage(LOCKED_MESSAGE_PASSAGE_ID).getText(e.defName());
    }

    /** Sets the message to be appended to the description when the door is locked. */
    public void setLockedMessage(TextSource lockedMessage) {
        this.lockedMessage = lockedMessage;
    }

    private String getNoKeyMessage(Entity e) {
        return noKeyMessage != null
            ? noKeyMessage.getText(e.defName())
            : bundles.getPassage(NOKEY_MESSAGE_PASSAGE_ID).getText(e.defName());
    }

    /** Sets the message shown when the player attempts to lock or unlock the door without holding the key. */
    public void setNoKeyMessage(TextSource noKeyMessage) {
        this.noKeyMessage = noKeyMessage;
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
            return description + " " + getLockedMessage(e);
        else
            return description;
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        AttributeSet attr = e.getAttributes();
        actions.clear();
        if (key == null)
            attr.clear(LOCKED);
        if (attr.get(LOCKED)) {
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
            // note that in these cases we already know that key != null
            if (!gm.isInInventory(key)) {
                gm.println(getNoKeyMessage(e));
            } else {
                attr.toggle(LOCKED);
                gm.entityChanged(e);
            }
            return true;
        } else if (action.equals(SystemActions.OPEN) || action.equals(SystemActions.CLOSE)) {
            attr.toggle(CLOSED);
            if (attr.get(CLOSED)) {
                room1.setExit(pos1, null);
                room1.setExitLabel(pos1, closedExitLabel);
                room2.setExit(pos2, null);
                room2.setExitLabel(pos2, closedExitLabel);
            } else {
                room1.setExit(pos1, room2);
                room2.setExit(pos2, room1);
            }
            gm.entityChanged(e);
            gm.roomChanged(room1);
            gm.roomChanged(room2);
            return true;
        }

        return false;
    }
}
