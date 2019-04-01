package com.illcode.meterman2.model;

import com.illcode.meterman2.MMScript.ScriptedMethod;
import com.illcode.meterman2.text.TextSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static com.illcode.meterman2.MMLogging.logger;

public final class ScriptedRoomImpl implements RoomImpl
{
    private final EnumMap<RoomMethod,ScriptedMethod> scriptedRoomMethods;

    /**
     * Construct a scripted room implementation from methods given in a map.
     * @param id the ID of the script (for instance as defined in a bundle)
     * @param methodMap map from method name to scripted method
     */
    public ScriptedRoomImpl(String id, Map<String,ScriptedMethod> methodMap) {
        scriptedRoomMethods = new EnumMap<>(RoomMethod.class);
        for (RoomMethod rm : RoomMethod.values()) {
            ScriptedMethod sm = methodMap.get(rm.getMethodName());
            if (sm != null)
                scriptedRoomMethods.put(rm, sm);
        }
    }

    /**
     * Return a set of the methods defined in the script.
     */
    public EnumSet<RoomMethod> getScriptedRoomMethods() {
        EnumSet<RoomMethod> methodSet;
        // EnumSet.copyOf() requires that the passed collection have at least one element.
        if (scriptedRoomMethods.isEmpty())
            methodSet = EnumSet.noneOf(RoomMethod.class);
        else
            methodSet = EnumSet.copyOf(scriptedRoomMethods.keySet());
        return methodSet;
    }

    public String getDescription(Room r) {
        return invokeWithResultOrError(RoomMethod.GET_DESCRIPTION, String.class, "[error]", r);
    }

    public void setDescription(TextSource description) {
        // ignore attempts to set the description
    }

    public void entered(Room r, Room fromRoom) {
        invokeMethod(RoomMethod.ENTERED, r, fromRoom);
    }

    public boolean exiting(Room r, Room toRoom) {
        return invokeWithResultOrError(RoomMethod.EXITING, Boolean.class, Boolean.FALSE, r, toRoom);
    }

    public void eachTurn(Room r) {
        invokeMethod(RoomMethod.EACH_TURN, r);
    }

    public Object getState(Room r) {
        return invokeWithResultOrError(RoomMethod.GET_STATE, Object.class, null, r);
    }

    public void restoreState(Room r, Object state) {
        invokeMethod(RoomMethod.RESTORE_STATE, r, state);
    }

    public void gameStarting(Room r) {
        invokeMethod(RoomMethod.GAME_STARTING, r);
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
        if (m == null)
            return errorVal;
        else
            return m.invokeWithResultOrError(resultClass, errorVal, args);
    }
}
