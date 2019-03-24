package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.text.TextSource;

import java.util.*;

import static com.illcode.meterman2.MMLogging.logger;

public final class ScriptedEntityImpl implements EntityImpl
{
    private final EnumMap<EntityMethod,ScriptedMethod> scriptedEntityMethods;

    /**
     * Construct a scripted entity implementation from methods given in a map.
     * @param id the ID of the script (for instance as defined in a bundle)
     * @param methodMap map from method name to scripted method
     */
    public ScriptedEntityImpl(String id, Map<String,ScriptedMethod> methodMap) {
        scriptedEntityMethods = new EnumMap<>(EntityMethod.class);
        for (EntityMethod em : EntityMethod.values()) {
            ScriptedMethod sm = methodMap.get(em.getMethodName());
            if (sm != null)
                scriptedEntityMethods.put(em, sm);
        }
    }

    /**
     * Return a set of the methods defined in the script.
     */
    public EnumSet<EntityMethod> getScriptedEntityMethods() {
        EnumSet<EntityMethod> methodSet;
        // EnumSet.copyOf() requires that the passed collection have at least one element.
        if (scriptedEntityMethods.isEmpty())
            methodSet = EnumSet.noneOf(EntityMethod.class);
        else
            methodSet = EnumSet.copyOf(scriptedEntityMethods.keySet());
        return methodSet;
    }

    // Note that if any of these methods are called, a ScriptedMethod should exist for that method,
    // since only methods in scriptedEntityMethods.keySet() were reported to be available; thus if
    // the map has no entry for a method, it is an error.

    public String getDescription(Entity e) {
        return invokeWithResultOrError(EntityMethod.GET_DESCRIPTION, String.class, "[error]", e);
    }

    public void setDescription(TextSource description) {
        // we ignore attempts to set the description.
    }

    public void lookInRoom(Entity e) {
        invokeMethod(EntityMethod.LOOK_IN_ROOM, e);
    }

    public void enterScope(Entity e) {
        invokeMethod(EntityMethod.ENTER_SCOPE, e);
    }

    public void exitingScope(Entity e) {
        invokeMethod(EntityMethod.EXITING_SCOPE, e);
    }

    public void taken(Entity e) {
        invokeMethod(EntityMethod.TAKEN, e);
    }

    public void dropped(Entity e) {
        invokeMethod(EntityMethod.DROPPED, e);
    }

    @SuppressWarnings("unchecked")
    public List<MMActions.Action> getActions(Entity e) {
        return invokeWithResultOrError(EntityMethod.GET_ACTIONS, List.class,
                                       Collections.<MMActions.Action>emptyList(), e);
    }

    public boolean processAction(Entity e, MMActions.Action action) {
        return invokeWithResultOrError(EntityMethod.PROCESS_ACTION, Boolean.class, Boolean.FALSE, e, action);
    }

    public boolean objectAction(Entity e, MMActions.Action action, Entity selectedEntity) {
        return invokeWithResultOrError(EntityMethod.OBJECT_ACTION, Boolean.class, Boolean.FALSE,
                                       e, action, selectedEntity);
    }

    public Object getState(Entity e) {
        return invokeWithResultOrError(EntityMethod.GET_STATE, Object.class, null, e);
    }

    public void restoreState(Entity e, Object state) {
        invokeMethod(EntityMethod.RESTORE_STATE, e, state);
    }

    // Invoke a method with no return value.
    private void invokeMethod(EntityMethod method, Object... args) {
        ScriptedMethod m = scriptedEntityMethods.get(method);
        if (m != null)
            m.invoke(args);
    }

    // Invoke a method that should return a non-null value.
    @SuppressWarnings("unchecked")
    private <T> T invokeWithResultOrError(EntityMethod method, Class<? extends T> resultClass,
                                          T errorVal, Object... args) {
        ScriptedMethod m = scriptedEntityMethods.get(method);
        T result = null;
        if (m != null) {
            try {
                result = resultClass.cast(m.invoke(args));
            } catch (ClassCastException ex) {
                logger.warning("ScriptedEntityImpl ClassCastException in " + method.getMethodName());
                result = null;
            }
        }
        return result != null ? result : errorVal;
    }
}
