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

    public static void init() {
        LOOK = Meterman2.actions.registerAction("Look");
        WAIT = Meterman2.actions.registerAction("Wait");
        OPEN = Meterman2.actions.registerAction("Open");
        CLOSE = Meterman2.actions.registerAction("Close");
        LOCK = Meterman2.actions.registerAction("Lock");
        UNLOCK = Meterman2.actions.registerAction("Unlock");
        Meterman2.actions.markSystemActionsDone();
    }
}
