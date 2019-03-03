package com.illcode.meterman2;

import static com.illcode.meterman2.MMActions.Action;

public final class SystemActions
{
    public static Action LOOK;
    public static Action WAIT;

    public static void init() {
        LOOK = Meterman2.actions.registerAction("Look");
        WAIT = Meterman2.actions.registerAction("Wait");
        Meterman2.actions.markSystemActionsDone();
    }
}
