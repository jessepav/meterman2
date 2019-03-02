package com.illcode.meterman2;

import bsh.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

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

    // Used to gather up the output emitted by a script using the out() BeanShell method.
    private StringBuilder outputBuilder;

    public MMScript() {
        intr = new Interpreter();
        systemNameSpace = intr.getNameSpace();
        unimportUnneededDefaults(systemNameSpace);
        importMMPackages(systemNameSpace);
        gameNameSpace = new NameSpace(systemNameSpace, "gameNameSpace");
        initSystemNameSpace();
    }

    /** Free any resources allocated by this MMScript instance. */
    public void dispose() {
        gameNameSpace = null;
        systemNameSpace = null;
        intr = null;
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

    private void initSystemNameSpace() {
        StringBuilder outputBuilder = new StringBuilder(1024);
        try {
            intr.set("outputBuilder", outputBuilder);
            intr.eval(Utils.getStringResource("bsh/system-script.bsh"));
        } catch (EvalError ex) {
            logger.log(Level.WARNING, "MMScript error:", ex);
        }
    }

    /**
     * Add a map of game-state bindings to our game namespace.
     * @param bindings name to game-state object mapping
     */
    public void putBindings(Map<String,Object> bindings) {
        try {
            for (Map.Entry<String,Object> entry : bindings.entrySet()) {
                Object value = entry.getValue();
                String name = entry.getKey();
                gameNameSpace.setTypedVariable(name, value.getClass(), value, null);
            }
        } catch (UtilEvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
        }
    }

    /** Clear all game-state bindings from our game namespace. */
    public void clearBindings() {
        gameNameSpace.clear();
    }

    /**
     * Evaluate a script and retrieve methods declared in it.
     * <p/>
     * NOTE: scripted methods should not use primitive types in their parameter lists; instead,
     * use the wrapper classes (Integer, etc.).
     * @param id script ID (aka name)
     * @param source script source
     * @return a list of ScriptedMethod instances that can be used to query and
     *         invoke the methods declared in the script.
     */
    public List<ScriptedMethod> evalScript(String id, String source) {
        NameSpace ns = new NameSpace(gameNameSpace, id);
        List<ScriptedMethod> methods = null;
        try {
            intr.eval(source, ns);
            BshMethod[] bshMethods = ns.getMethods();
            methods = new ArrayList<>(bshMethods.length);
            for (BshMethod m : bshMethods)
                methods.add(new ScriptedMethod(m));
        } catch (EvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
            methods = Collections.emptyList();
        }
        return methods;
    }

    /**
     * A ScriptedMethod encapsulates the particular machinery of our script engine,
     * and allows users to query and invoke methods defined in a script.
     */
    public class ScriptedMethod
    {
        private final BshMethod bshMethod;

        private ScriptedMethod(BshMethod bshMethod) {
            this.bshMethod = bshMethod;
        }

        /** Return the name of the method. */
        public String getName() {
            return bshMethod.getName();
        }

        /**
         * Invoke the method, passing a list of arguments.
         * @param args arguments to the method
         * @return a pair comprising the return value of the scripted method (as the "left" member of the pair),
         *         and the string output emitted (as the "right" member of the pair)
         */
        public Pair<Object,String> invoke(Object... args) {
            Object retval;
            String output;
            try {
                Object[] bshArgs = new Object[args.length];  // we need to wrap null values as Primitive.NULL
                for (int i = 0; i < args.length; i++)
                    bshArgs[i] = args[i] == null ? Primitive.NULL : args[i];
                outputBuilder.setLength(0);
                retval = bshMethod.invoke(bshArgs, intr);
                output = outputBuilder.toString();
                outputBuilder.setLength(0);
            } catch (EvalError err) {
                logger.log(Level.WARNING, "MMScript error:", err);
                retval = null;
                output = null;
            }
            return Pair.of(retval, output);
        }
    }
}
