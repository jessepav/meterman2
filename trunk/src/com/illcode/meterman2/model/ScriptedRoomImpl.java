package com.illcode.meterman2.model;

import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.Meterman2;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import static com.illcode.meterman2.MMLogging.logger;

public final class ScriptedRoomImpl implements RoomImpl
{
    private final EnumMap<RoomMethod,ScriptedMethod> scriptedRoomMethods;

    private EnumSet<RoomMethod> methodSet;  // cache for getScriptedRoomMethods()

    /**
     * Construct a scripted room implementation whose methods are defined in a script.
     * @param id the ID of script (for instance as given in a bundle)
     * @param source script source
     */
    public ScriptedRoomImpl(String id, String source) {
        scriptedRoomMethods = new EnumMap<>(RoomMethod.class);
        List<ScriptedMethod> scriptedMethods = Meterman2.script.evalScript(id, source);
        for (ScriptedMethod sm : scriptedMethods) {  // methods defined in the script
            for (RoomMethod rm : RoomMethod.values()) {  // our "method name" enum
                if (sm.getName().equals(rm.getMethodName())) {
                    scriptedRoomMethods.put(rm, sm);
                    break;
                }
            }
        }
    }

    /**
     * Return a set of the methods defined in the script.
     */
    public EnumSet<RoomMethod> getScriptedRoomMethods() {
        if (methodSet == null) {
            // EnumSet.copyOf() requires that the passed collection have at least one element.
            if (scriptedRoomMethods.isEmpty())
                methodSet = EnumSet.noneOf(RoomMethod.class);
            else
                methodSet = EnumSet.copyOf(scriptedRoomMethods.keySet());
        }
        return methodSet;
    }

    public String getDescription(Room r) {
        return invokeWithResultOrError(RoomMethod.GET_DESCRIPTION, String.class, "[error]", r);
    }

    public void entered(Room r, Room fromRoom) {
        invokeMethod(RoomMethod.ENTERED, r, fromRoom);
    }

    public boolean exiting(Room r, Room toRoom) {
        return invokeWithResultOrError(RoomMethod.EXITING, Boolean.class, Boolean.FALSE, r, toRoom);
    }

    // Invoke a method with no return value.
    private void invokeMethod(RoomMethod method, Object... args) {
        ScriptedMethod m = scriptedRoomMethods.get(method);
        if (m != null)
            m.invoke(args);
    }

    // Invoke a method that should return a non-null value.
    @SuppressWarnings("unchecked")
    private <T> T invokeWithResultOrError(RoomMethod method, Class<? extends T> resultClass,
                                          T errorVal, Object... args) {
        ScriptedMethod m = scriptedRoomMethods.get(method);
        T result = null;
        if (m != null) {
            try {
                result = resultClass.cast(m.invoke(args));
            } catch (ClassCastException ex) {
                logger.warning("ScriptedRoomImpl ClassCastException in " + method.getMethodName());
                result = null;
            }
        }
        return result != null ? result : errorVal;
    }
}
