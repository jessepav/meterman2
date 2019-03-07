package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;
import org.apache.commons.lang3.tuple.Pair;

import static com.illcode.meterman2.Meterman2.gm;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
        for (ScriptedMethod sm : scriptedMethods) {
            for (EntityMethod em : EntityMethod.values()) {
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

    public String getName(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.GET_NAME);
        if (m != null)
            return (String) m.invoke(e).getLeft();
        else
            return e.getName();
    }

    public String getDescription(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.GET_DESCRIPTION);
        if (m != null)
            return (String) m.invoke(e).getLeft();
        else
            return e.getDescription();
    }

    public void lookInRoom(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.LOOK_IN_ROOM);
        if (m != null)
            m.invoke(e);
        else
            e.lookInRoom();
    }

    public void enterScope(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.ENTER_SCOPE);
        if (m != null)
            m.invoke(e);
        else
            e.enterScope();
    }

    public void exitingScope(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.EXITING_SCOPE);
        if (m != null)
            m.invoke(e);
        else
            e.exitingScope();
    }

    public void taken(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.TAKEN);
        if (m != null)
            m.invoke(e);
        else
            e.taken();
    }

    public void dropped(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.DROPPED);
        if (m != null)
            m.invoke(e);
        else
            e.dropped();
    }

    @SuppressWarnings("unchecked")
    public List<MMActions.Action> getActions(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.GET_ACTIONS);
        if (m != null)
            return (List<MMActions.Action>) m.invoke(e).getLeft();
        else
            return e.getActions();
    }

    public boolean processAction(Entity e, MMActions.Action action) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.PROCESS_ACTION);
        if (m != null)
            return ((Boolean) m.invoke(e).getLeft()).booleanValue();
        else
            return e.processAction(action);
    }
}
