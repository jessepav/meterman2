package com.illcode.meterman2.model;

import com.illcode.meterman2.GameManager;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemAttributes;
import com.illcode.meterman2.event.TurnListener;

import java.util.Collections;
import java.util.List;

/**
 * A room that can be, though is not necessarily, dark.
 * <p/>
 * If the room has the attribute {@link SystemAttributes#DARK} and there is no lightsource
 * in the room, then we return a different "dark name" and "dark exit name", and hide the
 * contents of the room.
 */
public class DarkRoom extends Room implements TurnListener
{
    protected String darkName;
    protected String darkExitName;

    private boolean wasDark;  // used to detect changes in darkness at the end of each turn.

    protected DarkRoom(String id, RoomImpl impl) {
        super(id, impl);
    }

    /** Create a dark room with the given ID and a dark room implemention. */
    public static DarkRoom create(String id) {
        return create(id, new DarkRoomImpl());
    }

    /** Create a dark room with the given ID and implemention. */
    public static DarkRoom create(String id, RoomImpl impl) {
        return new DarkRoom(id, impl);
    }

    public void setDarkName(String darkName) {
        this.darkName = darkName;
    }

    public void setDarkExitName(String darkExitName) {
        this.darkExitName = darkExitName;
    }

    @Override
    public String getName() {
        if (darkName != null && isDark())
            return darkName;
        else
            return super.getName();
    }

    @Override
    public String getExitName() {
        if (darkExitName != null && isDark())
            return darkExitName;
        else
            return super.getExitName();
    }

    @Override
    public List<Entity> getEntities() {
        if (isDark())
            return Collections.emptyList();
        else
            return super.getEntities();
    }

    public static boolean isDark(Room r) {
        return r instanceof DarkRoom && ((DarkRoom)r).isDark();
    }

    public boolean isDark() {
        // if we're not naturally dark, then we're definite not dark now.
        if (!getAttributes().get(SystemAttributes.DARK))
            return false;

        // Otherwise, let us see if something in the room is a light source.
        for (Entity e : getEntities()) {
            if (isLightSource(e)) {
                return false;
            } else if (e instanceof EntityContainer) {
                // we check only one level deep in containment, for the reasonable situation where,
                // say, a lamp is sitting on a shelf.
                EntityContainer c = (EntityContainer) e;
                if (!e.getAttributes().get(SystemAttributes.LOCKED))
                    for (Entity ce : c.getEntities())
                        if (isLightSource(ce))
                            return false;
            }
        }
        // In player inventory, we do not check for containment: a lamp in a bag doesn't light the room.
        for (Entity e : Meterman2.gm.getPlayer().getEntities())
            if (isLightSource(e))
                return false;

        // DARKNESS! Charley Murphy!
        return true;
    }

    private static boolean isLightSource(Entity e) {
        return e.getAttributes().get(SystemAttributes.LIGHTSOURCE);
    }

    @Override
    public void entered(Room fromRoom) {
        Meterman2.gm.addTurnListener(this);
        wasDark = isDark();
        super.entered(fromRoom);
    }

    @Override
    public boolean exiting(Room toRoom) {
        boolean blocked = super.exiting(toRoom);
        if (!blocked)
            Meterman2.gm.removeTurnListener(this);
        return blocked;
    }

    @Override
    public Object getState() {
        Object[] stateObj = new Object[2];
        stateObj[0] = Boolean.valueOf(wasDark);
        stateObj[1] = super.getState();
        return stateObj;
    }

    @Override
    public void restoreState(Object state) {
        Object[] stateObj = (Object[]) state;
        wasDark = ((Boolean) stateObj[0]).booleanValue();
        super.restoreState(stateObj[1]);
    }

    //region -- Implement TurnListener --
    public void turn() {
        boolean nowDark = isDark();
        if (wasDark != nowDark) {
            wasDark = nowDark;
            Meterman2.gm.roomChanged(this);
        }
    }

    public String getHandlerId() {
        return ROOM_EVENT_HANDLER_PREFIX + getId();
    }
    //endregion
}
