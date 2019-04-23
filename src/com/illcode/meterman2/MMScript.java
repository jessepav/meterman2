package com.illcode.meterman2;

import bsh.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
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
    /** A marker singleton to indicate that a method had a void return type. */
    public static final Object VOID_RETURN = new Object();

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

    // Used for push/pop binding.
    private Map<String,Deque<Object>> savedBindings;

    // Used to gather up the output emitted by a script using the out() BeanShell method.
    private StringBuilder outputBuilder;

    public MMScript() {
        intr = new Interpreter();
        systemNameSpace = intr.getNameSpace();
        unimportUnneededDefaults(systemNameSpace);
        initSystemNameSpace();
        gameNameSpace = new NameSpace(systemNameSpace, "gameNameSpace");
        savedBindings = new HashMap<>();
    }

    /** Free any resources allocated by this MMScript instance. */
    public void dispose() {
        savedBindings = null;
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
        systemNameSpace.importPackage("com.illcode.meterman2");
        systemNameSpace.importPackage("com.illcode.meterman2.model");
        systemNameSpace.importClass("com.illcode.meterman2.MMActions.Action");
        systemNameSpace.importClass("com.illcode.meterman2.model.TopicMap.Topic");
        systemNameSpace.importClass("com.illcode.meterman2.util.Dialogs.DialogPassage");
        systemNameSpace.importClass("com.illcode.meterman2.util.Dialogs.DialogSequence");

        outputBuilder = new StringBuilder(1024);
        try {
            systemNameSpace.setTypedVariable("outputBuilder", StringBuilder.class, outputBuilder, null);
            intr.eval(Utils.getStringResource("scripts/system-script.bsh"));
        } catch (UtilEvalError|EvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
        }
    }

    /**
     * Put a variable binding into the system namespace.
     * @param name name of the variable
     * @param value value; if null, the binding will be removed.
     */
    void putSystemBinding(String name, Object value) {
        putBindingImpl(name, value, systemNameSpace);
    }

    /** Remove a variable binding from the system namespace. */
    void removeSystemBinding(String name) {
        putBindingImpl(name, null, systemNameSpace);
    }

    /**
     * Put a variable binding into the game namespace.
     * @param name name of the variable
     * @param value value; if null, the binding will be removed.
     */
    public void putBinding(String name, Object value) {
        putBindingImpl(name, value, gameNameSpace);
    }

    /** Remove a variable binding from the game namespace. */
    public void removeBinding(String name) {
        putBindingImpl(name, null, gameNameSpace);
    }

    // Put a variable binding into a given namespace.
    private static void putBindingImpl(String name, Object value, NameSpace ns) {
        try {
            ns.unsetVariable(name);
            if (value != null)
                ns.setTypedVariable(name, value.getClass(), value, null);
        } catch (UtilEvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
        }
    }

    /**
     * Saves the current value of a binding in the game namespace and sets a new one.
     * @param name variable name
     * @param value new value; if null, the binding will be removed.
     */
    public void pushBinding(String name, Object value) {
        Object oldVal = null;
        try {
            oldVal = gameNameSpace.getVariable(name, false);
            if (oldVal == Primitive.VOID)
                oldVal = null;
        } catch (UtilEvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
        }
        Deque<Object> savedVals = savedBindings.get(name);
        if (savedVals == null) {
            savedVals = new LinkedList<>();
            savedBindings.put(name, savedVals);
        }
        savedVals.push(oldVal);
        putBinding(name, value);
    }

    /**
     * Restores the value of a binding in the game namespace saved with {@link #pushBinding(String, Object)}.
     * @param name variable name
     */
    public void popBinding(String name) {
        Object previousVal = null;
        Deque<Object> savedVals = savedBindings.get(name);
        if (savedVals != null && !savedVals.isEmpty())
            previousVal = savedVals.pop();
        putBinding(name, previousVal);
    }

    /**
     * Put a map of game-state bindings into our game namespace.
     * @param bindings name to game-state object mapping
     */
    public void putBindings(Map<String,Object> bindings) {
        try {
            for (Map.Entry<String,Object> entry : bindings.entrySet()) {
                Object value = entry.getValue();
                String name = entry.getKey();
                gameNameSpace.unsetVariable(name);
                if (value != null)
                    gameNameSpace.setTypedVariable(name, value.getClass(), value, null);
            }
        } catch (UtilEvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
        }
    }

    /**
     * Import a package into the game namespace.
     * @param name package name
     */
    public void importPackage(String name) {
        gameNameSpace.importPackage(name);
    }

    /**
     * Import a class into the game namespace.
     * @param name class name
     */
    public void importClass(String name) {
        gameNameSpace.importClass(name);
    }

    /** Clear all game-state bindings from our game namespace. */
    public void clearBindings() {
        gameNameSpace.clear();
    }

    /**
     * Evaluates a script in the context of the system namespace.
     * @param source script source
     * @return result of the evaluation of the last statement or expression in the source
     */
    Object evalSystemScript(String source) {
        return evalScript(source, systemNameSpace);
    }

    /**
     * Evaluates a script in the context of the game namespace. Games can call this method
     * to populate the parent namespace of all game scripts with utility methods, etc.
     * @param source script source
     * @return result of the evaluation of the last statement or expression in the source
     */
    public Object evalScript(String source) {
        return evalScript(source, gameNameSpace);
    }

    // Evaluate a script in a given namespace.
    private Object evalScript(String source, NameSpace ns) {
        try {
            return intr.eval(source, ns);
        } catch (EvalError err) {
            logger.log(Level.WARNING, "MMScript error:", err);
            return null;
        }
    }

    /**
     * Evaluate a script and retrieve methods declared in it. The script is evaluated in a new
     * namespace that is a child of the game namespace.
     * @param id script ID (aka name)
     * @param source script source
     * @return a list of ScriptedMethod instances that can be used to query and
     *         invoke the methods declared in the script.
     */
    public List<ScriptedMethod> getScriptedMethods(String id, String source) {
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
         * Put a variable binding into the method's declaring namespace. The body of this method, and any
         * other methods declared in the same script, may then refer to this variable during execution.
         * @param name name of the variable
         * @param value value; if null, the binding will be removed.
         */
        public void putBinding(String name, Object value) {
            putBindingImpl(name, value, ns);
        }

        /** Remove a variable binding from the method's declaring namespace. */
        public void removeBinding(String name) {
            putBindingImpl(name, null, ns);
        }

        /**
         * Invoke the method, passing a list of arguments, and return the emitted text.
         * To emit text, the script may call the {@code out(String text)} and {@code outPassage(String id)}
         * methods defined in the system namespace.
         * @param args arguments to the method
         * @return the string output emitted
         */
        public String invokeGetOutput(Object... args) {
            String output;
            outputBuilder.setLength(0);
            try {
                bshMethod.invoke(getBshArgs(args), intr);
                output = outputBuilder.toString();
            } catch (Throwable t) {
                output = "MMScript error: " + t.getMessage();
                logger.warning(output);
            }
            outputBuilder.setLength(0);
            return output;
        }

        /**
         * Invoke the method, passing a list of arguments.
         * @param args arguments to the method
         * @return the return value of the method. Returns {@code MMScript.VOID_RETURN} for a void
         *          return-type method.
         */
        public Object invoke(Object... args) {
            Object result;
            try {
                result = bshMethod.invoke(getBshArgs(args), intr);
                if (result instanceof Primitive) {
                    if (result == Primitive.VOID)
                        result = VOID_RETURN;
                    else
                        result = ((Primitive)result).getValue();
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "MMScript error:", t);
                result = null;
            }
            outputBuilder.setLength(0);  // in case the script output something anyway
            return result;
        }

        private Object[] getBshArgs(Object[] args) {
            // optimize the case where no new array creation is required
            if (ArrayUtils.indexOf(args, null) == -1)
                return args;

            Object[] bshArgs = new Object[args.length];  // we need to wrap null values as Primitive.NULL
            for (int i = 0; i < args.length; i++)
                bshArgs[i] = args[i] == null ? Primitive.NULL : args[i];
            return bshArgs;
        }

        /**
         * Invokes the method with an expected return type.
         * @param resultClass expected class of the method return value
         * @param errorVal value to return if the method return value is not of the expected class
         * @param args arguments to the method
         * @param <T> type of the expected return value
         * @return result of the method invocation, or error value
         */
        public <T> T invokeWithResultOrError(Class<? extends T> resultClass, T errorVal, Object... args) {
            T result = null;
            try {
                result = resultClass.cast(invoke(args));
            } catch (ClassCastException ex) {
                logger.warning("ClassCastException in ScriptedMethod " + getName());
            }
            return result != null ? result : errorVal;
        }
    }
}
