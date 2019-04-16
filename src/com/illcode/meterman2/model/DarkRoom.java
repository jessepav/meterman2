package com.illcode.meterman2.model;

import com.illcode.meterman2.*;
import com.illcode.meterman2.text.TextSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.illcode.meterman2.GameUtils.getPassage;

/**
 * A room that is naturally dark, unless a light source is found within.
 */
public class DarkRoom extends Room implements DarkAwareRoom
{
    protected String darkName;
    protected TextSource darkDescription;

    protected List<Entity> darkEntities;
    protected MMScript.ScriptedMethod getDarkEntitiesMethod;

    protected DarkRoom(String id, RoomImpl impl) {
        super(id, impl);
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

    public void setDarkDescription(TextSource darkDescription) {
        this.darkDescription = darkDescription;
    }

    public String getDarkName() {
        return darkName != null ? darkName : getName();
    }

    public String getDarkDescription() {
        GameUtils.pushBinding("room", this);
        final String desc = darkDescription != null ? darkDescription.getText() : getPassage("darkroom-description");
        GameUtils.popBinding("room");
        return desc;
    }

    @SuppressWarnings("unchecked")
    public List<Entity> getDarkEntities() {
        if (getDarkEntitiesMethod != null)
            return getDarkEntitiesMethod.invokeWithResultOrError(List.class, Collections.emptyList(), this);
        else
            return getDarkEntitiesImpl();
    }

    /**
     * Sets the list of entities that will be returned if the room is
     * dark and no scripted <tt>getDarkEntities()</tt> method is defined.
     * <p/>
     * The items in this list should not be <tt>TAKEABLE</tt> or <tt>MOVEABLE</tt>, since this darkEntities
     * list doesn't factor into the game's containment mechanism: after taking something, the user will see
     * the entity both in the dark room's list and in his inventory.
     * @param darkEntities list of entities; if null, our dark entities will be an empty list.
     */
    public void setDarkEntities(List<Entity> darkEntities) {
        this.darkEntities = darkEntities;
    }

    /**
     * Subclasses may override this method to return something other than an empty list
     * or the <tt>darkEntities</tt> list when the room is dark.
     * @return list of entities found here when dark
     */
    protected List<Entity> getDarkEntitiesImpl() {
        return darkEntities != null ? darkEntities : Collections.<Entity>emptyList();
    }

    protected void checkDarkness() {
        boolean wasDark = getAttributes().get(SystemAttributes.DARK);
        boolean nowDark = false;
        checkDark: {
            // Let us see if something in the room is a light source.
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
            if (Meterman2.gm.getCurrentRoom() == this)
                for (Entity e : Meterman2.gm.getPlayer().getEntities())
                    if (isLightSource(e))
                        break checkDark;

            nowDark = true;  // DARKNESS! Charley Murphy!
        }
        if (wasDark != nowDark) {
            getAttributes().set(SystemAttributes.DARK, nowDark);
            Meterman2.gm.roomChanged(this);
        }
    }

    private static boolean isLightSource(Entity e) {
        return e.getAttributes().get(SystemAttributes.LIGHTSOURCE);
    }

    /**
     * Set scripted methods to be used by this dark room.
     * <p/>
     * It looks for methods with these names (and implicit signatures) in the passed method map:
     * <dl>
     *     <dt>{@code List getDarkEntities(DarkRoom r)}</dt>
     *     <dd>called to return the list of entities found in the room when it's dark,
     *         in place of the normal Room.getEntities()</dd>
     * </dl>
     * @param methodMap map from method name to scripted method. If null, all of our scripted methods
     * will be cleared
     */
    public void setScriptedMethods(Map<String,MMScript.ScriptedMethod> methodMap) {
        if (methodMap != null) {
            getDarkEntitiesMethod = methodMap.get("getDarkEntities");
        } else {
            getDarkEntitiesMethod = null;
        }
    }

    @Override
    public void eachTurn() {
        checkDarkness();
        super.eachTurn();
    }
}
