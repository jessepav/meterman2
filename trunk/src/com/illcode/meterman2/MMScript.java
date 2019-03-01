package com.illcode.meterman2;

import bsh.Interpreter;
import bsh.NameSpace;
import bsh.This;

/**
 * This class handles Meterman's interaction with a scripting engine, in our case BeanShell.
 * <p/>
 * It is <em>not</em> thread-safe!
 *
 */
public final class MMScript
{
    /**
     * The global BeanShell interpreter that is created once upon construction and used
     * for all script evaluation.
     */
    private Interpreter intr;

    /**
     * The NameSpace that holds Meterman system imports, utility methods, and references
     * to the various manager classes.
     */
    private NameSpace systemNameSpace;

    /**
     * The NameSpace used by an individual game, which is used as the parent namespace
     * for all scripted objects. This namespace is populated by the game itself when it's
     * loaded and should not be modified by scripts because it's not persisted.
     */
    private NameSpace gameNameSpace;

    public MMScript() {
        intr = new Interpreter();
        systemNameSpace = intr.getNameSpace();
        unimportUnneededDefaults(systemNameSpace);
        importMMPackages(systemNameSpace);
        gameNameSpace = new NameSpace(systemNameSpace, "gameNameSpace");
    }

    private void unimportUnneededDefaults(NameSpace ns) {
        ns.unimportPackage("javax.swing.event");
        ns.unimportPackage("javax.swing");
        ns.unimportPackage("java.awt.event");
        ns.unimportPackage("java.awt");
        ns.unimportPackage("java.net");
        ns.unimportPackage("java.io");
        ns.unimportCommands("/bsh/commands");
    }

    private void importMMPackages(NameSpace ns) {
        ns.importPackage("com.illcode.meterman2");
        ns.importPackage("com.illcode.meterman2.model");
    }
}
