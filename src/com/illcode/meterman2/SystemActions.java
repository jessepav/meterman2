package com.illcode.meterman2;

import static com.illcode.meterman2.MMActions.Action;

public final class SystemActions
{
    public static Action LOOK;
    public static Action WAIT;
    public static Action OPEN;
    public static Action CLOSE;
    public static Action LOCK;
    public static Action UNLOCK;
    public static Action CONTAINER_PUT;
    public static Action CONTAINER_TAKE;
    public static Action CONTAINER_EXAMINE;

    public static void init() {
        LOOK = Meterman2.actions.registerAction("Look");
        WAIT = Meterman2.actions.registerAction("Wait");
        OPEN = Meterman2.actions.registerAction("Open");
        CLOSE = Meterman2.actions.registerAction("Close");
        LOCK = Meterman2.actions.registerAction("Lock");
        UNLOCK = Meterman2.actions.registerAction("Unlock");
        CONTAINER_PUT = Meterman2.actions.registerAction("Put Item %s");
        CONTAINER_TAKE = Meterman2.actions.registerAction("Take Item %s");
        CONTAINER_EXAMINE = Meterman2.actions.registerAction("Examine Items");
        Meterman2.actions.markSystemActionsDone();
    }
}
