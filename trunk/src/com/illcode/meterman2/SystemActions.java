package com.illcode.meterman2;

import static com.illcode.meterman2.MMActions.Action;
import static com.illcode.meterman2.Meterman2.actions;

public final class SystemActions
{
    public static Action LOOK;
    public static Action WAIT;
    public static Action EXAMINE;
    public static Action TAKE;
    public static Action DROP;
    public static Action EQUIP;
    public static Action UNEQUIP;
    public static Action OPEN;
    public static Action CLOSE;
    public static Action LOCK;
    public static Action UNLOCK;
    public static Action CONTAINER_PUT;
    public static Action CONTAINER_TAKE;
    public static Action CONTAINER_LOOK_IN;

    public static void init() {
        // Don't set shortcuts yet because the UI doesn't exist
        GameUtils.registerActions(Meterman2.bundles.getElement("system-actions"), true, false);

        LOOK = actions.getAction("LOOK");
        WAIT = actions.getAction("WAIT");
        EXAMINE = actions.getAction("EXAMINE");
        TAKE = actions.getAction("TAKE");
        DROP = actions.getAction("DROP");
        EQUIP = actions.getAction("EQUIP");
        UNEQUIP = actions.getAction("UNEQUIP");
        OPEN = actions.getAction("OPEN");
        CLOSE = actions.getAction("CLOSE");
        LOCK = actions.getAction("LOCK");
        UNLOCK = actions.getAction("UNLOCK");
        CONTAINER_PUT = actions.getAction("CONTAINER_PUT");
        CONTAINER_TAKE = actions.getAction("CONTAINER_TAKE");
        CONTAINER_LOOK_IN = actions.getAction("CONTAINER_LOOK_IN");
    }
}
