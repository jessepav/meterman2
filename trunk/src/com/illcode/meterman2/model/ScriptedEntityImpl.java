package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;
import org.apache.commons.lang3.tuple.Pair;

import static com.illcode.meterman2.Meterman2.gm;

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

    public String getName(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.GET_NAME);
        if (m != null)
            try {
                return (String) m.invoke(e).getLeft();
            } catch (Exception e1) {
                logger.log(Level.WARNING, "ScriptedEntityImpl exception:", e1);
                return "[getName error]";
            }
        else
            return e.getName();
    }

    public String getDescription(Entity e) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.GET_DESCRIPTION);
        if (m != null)
            try {
                return (String) m.invoke(e).getLeft();
            } catch (Exception e1) {
                logger.log(Level.WARNING, "ScriptedEntityImpl exception:", e1);
                return "[getDescription error]";
            }
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
            try {
                return (List<MMActions.Action>) m.invoke(e).getLeft();
            } catch (Exception e1) {
                logger.log(Level.WARNING, "ScriptedEntityImpl exception:", e1);
                return Collections.emptyList();
            }
        else
            return e.getActions();
    }

    public boolean processAction(Entity e, MMActions.Action action) {
        ScriptedMethod m = scriptedEntityMethods.get(EntityMethod.PROCESS_ACTION);
        if (m != null)
            try {
                return ((Boolean) m.invoke(e).getLeft()).booleanValue();
            } catch (Exception e1) {
                logger.log(Level.WARNING, "ScriptedEntityImpl exception:", e1);
                return false;
            }
        else
            return e.processAction(action);
    }
}
