package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import static com.illcode.meterman2.MMLogging.logger;

public class ScriptedEntityImpl implements EntityImpl
{
    private final EnumMap<EntityMethod,ScriptedMethod> scriptedEntityMethods;

    private EnumSet<EntityMethod> methodSet;  // cache for getScriptedEntityMethods()

    /**
     * Construct a scripted entity implementation whose methods are defined in a script.
     * @param id the ID of script (for instance as given in a bundle)
     * @param source script source
     */
    public ScriptedEntityImpl(String id, String source) {
        scriptedEntityMethods = new EnumMap<>(EntityMethod.class);

        List<ScriptedMethod> scriptedMethods = Meterman2.script.evalScript(id, source);
        for (ScriptedMethod sm : scriptedMethods) {  // methods defined in the script
            for (EntityMethod em : EntityMethod.values()) {  // our "method name" enum
                if (sm.getName().equals(em.getMethodName())) {
                    scriptedEntityMethods.put(em, sm);
                    break;
                }
            }
        }
    }

    /**
     * Return a set of the methods defined in the script.
     */
    public EnumSet<EntityMethod> getScriptedEntityMethods() {
        if (methodSet == null) {
            // EnumSet.copyOf() requires that the passed collection have at least one element.
            if (scriptedEntityMethods.isEmpty())
                methodSet = EnumSet.noneOf(EntityMethod.class);
            else
                methodSet = EnumSet.copyOf(scriptedEntityMethods.keySet());
        }
        return methodSet;
    }

    // Note that if any of these methods are called, a ScriptedMethod should exist for that method,
    // since only methods in scriptedEntityMethods.keySet() were reported to be available; thus if
    // the map has no entry for a method, it is an error.

    public String getName(Entity e) {
        return getResultOrError(e, EntityMethod.GET_NAME, String.class, "[error]");
    }

    public String getDescription(Entity e) {
        return getResultOrError(e, EntityMethod.GET_DESCRIPTION, String.class, "[error]");
    }

    public void lookInRoom(Entity e) {
        invokeMethod(e, EntityMethod.LOOK_IN_ROOM);
    }

    public void enterScope(Entity e) {
        invokeMethod(e, EntityMethod.ENTER_SCOPE);
    }

    public void exitingScope(Entity e) {
        invokeMethod(e, EntityMethod.EXITING_SCOPE);
    }

    public void taken(Entity e) {
        invokeMethod(e, EntityMethod.TAKEN);
    }

    public void dropped(Entity e) {
        invokeMethod(e, EntityMethod.DROPPED);
    }

    @SuppressWarnings("unchecked")
    public List<MMActions.Action> getActions(Entity e) {
        return getResultOrError(e, EntityMethod.GET_ACTIONS, List.class, Collections.<MMActions.Action>emptyList());
    }

    public boolean processAction(Entity e, MMActions.Action action) {
        Boolean result = getResultOrError(e, EntityMethod.PROCESS_ACTION, Boolean.class, Boolean.FALSE);
        return result.booleanValue();
    }

    // Invoke a method with no return value.
    private void invokeMethod(Entity e, EntityMethod method) {
        ScriptedMethod m = scriptedEntityMethods.get(method);
        if (m != null)
            m.invoke(e);
    }

    // Invoke a method that should return a non-null value.
    @SuppressWarnings("unchecked")
    private <T> T getResultOrError(Entity e, EntityMethod method, Class<? extends T> clazz, T errorVal) {
        ScriptedMethod m = scriptedEntityMethods.get(method);
        T result = null;
        if (m != null) {
            try {
                result = clazz.cast(m.invoke());
            } catch (ClassCastException ex) {
                logger.warning("ScriptedEntityImpl ClassCastException in " + method.getMethodName());
                result = null;
            }
        }
        return result != null ? result : errorVal;
    }
}
