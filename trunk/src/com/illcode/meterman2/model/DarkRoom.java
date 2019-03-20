package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemAttributes;

import java.util.Collections;
import java.util.List;

/**
 * A room that can be, though is not necessarily, dark.
 * <p/>
 * If the room has the attribute {@link SystemAttributes#DARK} and there is no lightsource
 * in the room, then we return a different "dark name" and "dark exit name", and hide the
 * contents of the room.
 */
public class DarkRoom extends Room
{
    protected String darkName;
    protected String darkExitName;

    private boolean wasDark;  // used to detect changes in darkness
    private boolean firstDarkCheck;  // is this the first time we're checking if we're dark?

    protected DarkRoom(String id, RoomImpl impl) {
        super(id, impl);
        firstDarkCheck = true;
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

    public boolean isDark() {
        boolean nowDark = false;
        checkDark: {
            // if we're not naturally dark, then we're definite not dark now.
            if (!getAttributes().get(SystemAttributes.DARK))
                break checkDark;

            // Otherwise, let us see if something in the room is a light source.
            for (Entity e : getEntities()) {
                if (isLightSource(e)) {
                    break checkDark;
                } else if (e instanceof EntityContainer) {
                    // we check only one level deep in containment, for the reasonable situation where,
                    // say, a lamp is sitting on a shelf.
                    EntityContainer c = (EntityContainer) e;
                    if (!e.getAttributes().get(SystemAttributes.LOCKED))
                        for (Entity ce : c.getEntities())
                            if (isLightSource(ce))
                                break checkDark;
                }
            }
            // In player inventory, we do not check for containment: a lamp in a bag doesn't light the room.
            for (Entity e : Meterman2.gm.getPlayer().getEntities())
                if (isLightSource(e))
                    break checkDark;

            nowDark = true;  // DARKNESS! Charley Murphy!
        }
        boolean needRefresh = false;
        if (firstDarkCheck) {
            firstDarkCheck = false;
            wasDark = nowDark;
            needRefresh = true;
        } else if (wasDark != nowDark) {
            needRefresh = true;
            wasDark = nowDark;
        }
        if (needRefresh)
            Meterman2.gm.roomChanged(this);
        return nowDark;
    }

    public static boolean isDark(Room r) {
        return r instanceof DarkRoom && ((DarkRoom)r).isDark();
    }

    private static boolean isLightSource(Entity e) {
        return e.getAttributes().get(SystemAttributes.LIGHTSOURCE);
    }

    @Override
    public void entered(Room fromRoom) {
        wasDark = isDark();
        super.entered(fromRoom);
    }

    @Override
    public void eachTurn() {
        isDark();  // this will queue a room refresh if needed
        super.eachTurn();
    }

    @Override
    public Object getState() {
        Object[] stateObj = new Object[4];
        // We have to save the current values of name and exitName because if we're dark when state
        // is saved, the darkName and darkExitName will overwrite the real values of name and exitName
        // when state is restored.
        stateObj[0] = name;
        stateObj[1] = exitName;
        stateObj[2] = Boolean.valueOf(wasDark);
        stateObj[3] = super.getState();
        return stateObj;
    }

    @Override
    public void restoreState(Object state) {
        Object[] stateObj = (Object[]) state;
        name = (String) stateObj[0];
        exitName = (String) stateObj[1];
        wasDark = ((Boolean) stateObj[2]).booleanValue();
        super.restoreState(stateObj[3]);
    }
}
