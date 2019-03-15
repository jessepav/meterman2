package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.SystemAttributes;
import com.illcode.meterman2.text.TextSource;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.illcode.meterman2.model.GameUtils.hasAttr;
import static com.illcode.meterman2.model.GameUtils.setAttr;
import static com.illcode.meterman2.Meterman2.bundles;

/**
 * Implementation of door entities, is a special usage entity that can connect and disconnect two rooms,
 * depending on whether it is open.
 */
public class DoorImpl extends BaseEntityImpl
{
    public static final String LOCKED_MESSAGE_PASSAGE_ID = "door-locked-message";
    public static final String NOKEY_MESSAGE_PASSAGE_ID = "door-nokey-message";

    protected Entity entity;

    protected Room room1, room2;
    protected int pos1, pos2;
    protected TextSource lockedMessage;
    protected TextSource nokeyMessage;
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


    /** Let the DoorImpl know which entity we're implementing. */
    public void setEntity(Entity entity) {
        this.entity = entity;
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
     * Sets the positions (ex. {@link UIConstants#NW_BUTTON}) in the first and second room to be connected when the
     * door is unlocked.
     * @param pos1 exit position in first room
     * @param pos2 exit position in second room
     */
    public void setPositions(int pos1, int pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    private String getLockedMessage() {
        return lockedMessage != null
            ? lockedMessage.getText()
            : bundles.getPassage(LOCKED_MESSAGE_PASSAGE_ID).getText();
    }

    /** Sets the messages shown, in addition to the description, when the door is locked. */
    public void setLockedMessage(TextSource lockedMessage) {
        this.lockedMessage = lockedMessage;
    }

    private String getNokeyMessage() {
        return nokeyMessage != null
            ? nokeyMessage.getText()
            : bundles.getPassage(NOKEY_MESSAGE_PASSAGE_ID).getText();
    }

    /** Sets the message shown when the player attempts to lock or unlock the door without holding the key. */
    public void setNokeyMessage(TextSource nokeyMessage) {
        this.nokeyMessage = nokeyMessage;
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
        if (key == null)
            setLocked(false);

    }

    /** Returns true if the door is locked. */
    public boolean isLocked() {
        return hasAttr(entity, SystemAttributes.LOCKED);
    }

    /** Set whether the door is locked. */
    public void setLocked(boolean locked) {
        setAttr(entity, SystemAttributes.LOCKED, locked);
        if (locked)
            setClosed(true);  // a locked door is necessarily closed
    }

    /** Returns true if the door is closed. */
    public boolean isClosed() {
        return hasAttr(entity, SystemAttributes.CLOSED);
    }

    /** Set whether the door is open. */
    public void setClosed(boolean closed) {
        setAttr(entity, SystemAttributes.CLOSED, closed);
        if (!closed)
            setLocked(false);  // you cannot have an open, locked door
    }

    @Override
    public String getDescription(Entity e) {
        String description = super.getDescription(e);
        if (isLocked())
            return description + " " + getLockedMessage();
        else
            return description;
    }

    @Override
    public List<MMActions.Action> getActions(Entity e) {
        actions.clear();
        if (key == null)
            setLocked(false);
        if (isLocked()) {
            actions.add(SystemActions.UNLOCK);
        } else { // okay, we're unlocked
            if (isClosed()) { // closed but unlocked
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
        // TODO: Door.processAction()
        return super.processAction(e, action);
    }
}
