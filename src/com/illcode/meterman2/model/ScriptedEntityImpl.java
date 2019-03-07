package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;

import static com.illcode.meterman2.MMLogging.logger;

import java.util.*;
import java.util.logging.Level;

public class ScriptedEntityImpl implements EntityImpl
{
    private EnumMap<EntityMethod,ScriptedMethod> scriptedEntityMethods;

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
    public Set<EntityMethod> getScriptedEntityMethods() {
        return scriptedEntityMethods.keySet();
    }

    // Note that if any of these methods are called, a ScriptedMethod should exist for that method,
    // since only methods in scriptedEntityMethods.keySet() were reported to be available; thus if
    // the map has no entry for a method, it is an error.

    public String getName(Entity e) {
        return getResultOrError(e, EntityMethod.GET_NAME, "[error]");
    }

    public String getDescription(Entity e) {
        return getResultOrError(e, EntityMethod.GET_DESCRIPTION, "[error]");
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

    public List<MMActions.Action> getActions(Entity e) {
        return getResultOrError(e, EntityMethod.GET_ACTIONS, Collections.<MMActions.Action>emptyList());
    }

    public boolean processAction(Entity e, MMActions.Action action) {
        Boolean result = getResultOrError(e, EntityMethod.PROCESS_ACTION, Boolean.FALSE);
        return result.booleanValue();
    }

    private void invokeMethod(Entity e, EntityMethod method) {
        ScriptedMethod m = scriptedEntityMethods.get(method);
        if (m != null)
            m.invoke(e);
    }

    @SuppressWarnings("unchecked")
    private <T> T getResultOrError(Entity e, EntityMethod method, T errorVal) {
        ScriptedMethod m = scriptedEntityMethods.get(method);
        T result = null;
        if (m != null) {
            try {
                result = (T) m.invoke(e).getLeft();
            } catch (Exception ex) {
                logger.log(Level.WARNING, "ScriptedEntityImpl exception:", ex);
                result = null;
            }
        }
        return result != null ? result : errorVal;
    }
}
