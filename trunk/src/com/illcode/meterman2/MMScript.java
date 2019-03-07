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
        initSystemNameSpace();
        gameNameSpace = new NameSpace(systemNameSpace, "gameNameSpace");
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

    private void initSystemNameSpace() {
        systemNameSpace.importPackage("com.illcode.meterman2.model");
        systemNameSpace.importClass("com.illcode.meterman2.MMActions.Action");

        outputBuilder = new StringBuilder(1024);
        try {
            systemNameSpace.setTypedVariable("outputBuilder", StringBuilder.class, outputBuilder, null);
            intr.eval(Utils.getStringResource("scripts/system-script.bsh"));
        } catch (UtilEvalError|EvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
        }
    }

    /** Add a binding to the system namespace. */
    void addSystemBinding(String name, Object value) {
        try {
            systemNameSpace.setTypedVariable(name, value.getClass(), value, null);
        } catch (UtilEvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
        }
    }

    /**
     * Add a map of game-state bindings to our game namespace.
     * @param bindings name to game-state object mapping
     */
    public void putGameBindings(Map<String,Object> bindings) {
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
     * Evaluates a script in the context of the game namespace. Games can call this method
     * to populate the parent namespace of all game scripts with utility methods, etc.
     * @param source script source
     * @return result of the evaluation of the last statement or expression in the evaluated string
     */
    public Object evalGameScript(String source) {
        try {
            return intr.eval(source, gameNameSpace);
        } catch (EvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
            return null;
        }
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
                methods.add(new ScriptedMethod(m, ns));
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
        private final NameSpace ns;

        private ScriptedMethod(BshMethod bshMethod, NameSpace ns) {
            this.bshMethod = bshMethod;
            this.ns = ns;
        }

        /** Return the name of the method. */
        public String getName() {
            return bshMethod.getName();
        }

        /**
         * Set a variable in the method's declaring namespace. The body of this method, and any other methods
         * declared in the same script, may then refer to this variable during execution.
         * @param name name of the variable
         * @param value value of the variable
         */
        public void setVariable(String name, Object value) {
            try {
                ns.setTypedVariable(name, value.getClass(), value, null);
            } catch (UtilEvalError err) {
                logger.log(Level.WARNING, "MMScript error:", err);
            }
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
                retval = null;
                output = "MMScript error: " + err.getMessage();
                logger.warning(output);
            }
            return Pair.of(retval, output);
        }
    }
}
