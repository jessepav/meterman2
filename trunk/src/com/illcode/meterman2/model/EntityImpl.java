package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions.Action;

import java.util.List;

/**
 * Defines an implementation for entity operation, used for both entity implementation instances
 * and delegates.
 * <p/>
 * The methods parallel those of {@link Entity}, with the addition of a parameter indicating
 * the entity whose operations we should implement.
 */
public interface EntityImpl
{
    /** See {@link Entity#getName()}*/
    String getName(Entity e);

    /** See {@link Entity#getDescription()} */
    String getDescription(Entity e);

    /** See {@link Entity#lookInRoom()} */
    void lookInRoom(Entity e);

    /** See {@link Entity#enterScope()} */
    void enterScope(Entity e);

    /** See {@link Entity#exitingScope()} */
    void exitingScope(Entity e);

    /** See {@link Entity#taken()} */
    void taken(Entity e);

    /** See {@link Entity#dropped()} */
    void dropped(Entity e);

    /** See {@link Entity#getActions()} */
    List<Action> getActions(Entity e);

    /** See {@link Entity#processAction(MMActions.Action)} */
    boolean processAction(Entity e, Action action);

    /**
     * Used to indicate which methods of an Entity will be delegated.
     */
    enum EntityMethod
    {
        GET_NAME("getName"), GET_DESCRIPTION("getDescription"), LOOK_IN_ROOM("lookInRoom"),
        ENTER_SCOPE("enterScope"), EXITING_SCOPE("exitingScope"), TAKEN("taken"), DROPPED("dropped"),
        GET_ACTIONS("getActions"), PROCESS_ACTION("processAction");

        private final String methodName;

        EntityMethod(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }
    }
}
