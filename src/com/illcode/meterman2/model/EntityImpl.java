package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.text.TextSource;

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
    /** See {@link Entity#getDescription()} */
    String getDescription(Entity e);

    /** Sets the description. Some implementations, for instance scripted implementations,
     *  may ignore calls to this method. */
    void setDescription(TextSource description);

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

    /** See {@link Entity#processAction(Action)} */
    boolean processAction(Entity e, Action action);

    /** See {@link Entity#getState()} */
    Object getState(Entity e);

    /** See {@link Entity#restoreState(Object)} */
    void restoreState(Entity e, Object state);

    /**
     * Used to indicate which methods of an Entity will be delegated.
     */
    enum EntityMethod
    {
        GET_DESCRIPTION("getDescription"), LOOK_IN_ROOM("lookInRoom"),
        ENTER_SCOPE("enterScope"), EXITING_SCOPE("exitingScope"), TAKEN("taken"), DROPPED("dropped"),
        GET_ACTIONS("getActions"), PROCESS_ACTION("processAction"),
        GET_STATE("getState"), RESTORE_STATE("restoreState");

        private final String methodName;

        EntityMethod(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }
    }
}
