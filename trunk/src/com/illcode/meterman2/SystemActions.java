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
        LOOK = actions.registerSystemAction("LOOK", "Look");
        WAIT = actions.registerSystemAction("WAIT", "Wait");
        EXAMINE = actions.registerSystemAction("EXAMINE", "Examine");
        TAKE = actions.registerSystemAction("TAKE", "Take");
        DROP = actions.registerSystemAction("DROP", "Drop");
        EQUIP = actions.registerSystemAction("EQUIP", "Equip");
        UNEQUIP = actions.registerSystemAction("UNEQUIP", "Unequip");
        OPEN = actions.registerSystemAction("OPEN", "Open");
        CLOSE = actions.registerSystemAction("CLOSE", "Close");
        LOCK = actions.registerSystemAction("LOCK", "Lock");
        UNLOCK = actions.registerSystemAction("UNLOCK", "Unlock");
        CONTAINER_PUT = actions.registerSystemAction("CONTAINER_PUT", "Put Item %s");
        CONTAINER_TAKE = actions.registerSystemAction("CONTAINER_TAKE", "Take Item %s");
        CONTAINER_LOOK_IN = actions.registerSystemAction("CONTAINER_LOOK_IN", "Look %s");
    }
}
