package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemAttributes;
import com.illcode.meterman2.SystemMessages;
import com.illcode.meterman2.text.TextSource;

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
    protected TextSource darkDescription;

    private boolean wasDark;  // used to detect changes in darkness
    private boolean firstDarkCheck;  // is this the first time we're checking if we're dark?

    protected DarkRoom(String id, RoomImpl impl) {
        super(id, impl);
        firstDarkCheck = true;
    }

    /** Create a dark room with the given ID and a dark room implemention. */
    public static DarkRoom create(String id) {
        return create(id, new BaseRoomImpl());
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

    public void setDarkDescription(TextSource darkDescription) {
        this.darkDescription = darkDescription;
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
    public String getDescription() {
        if (isDark())
            return darkDescription != null ? darkDescription.getText() :
                Meterman2.bundles.getPassage(SystemMessages.DARKROOM_DESCRIPTION).getText();
        else
            return super.getDescription();
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
    public void eachTurn() {
        isDark();  // this will queue a room refresh if needed
        super.eachTurn();
    }
}
