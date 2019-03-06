package com.illcode.meterman2.model;

import com.illcode.meterman2.MMActions;

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

    /** See {@link Entity#getRoom()} */
    Room getRoom(Entity e);

    /** See {@link Entity#setRoom(Room)} */
    void setRoom(Entity e, Room room);

    /** See {@link Entity#getActions()} */
    List<String> getActions(Entity e);

    /** See {@link Entity#processAction(MMActions.Action)} */
    boolean processAction(Entity e, MMActions.Action action);

    /**
     * Used to indicate which methods of an Entity will be delegated.
     */
    enum Methods
    {
        GET_NAME, GET_DESCRIPTION, LOOK_IN_ROOM, ENTER_SCOPE, EXITING_SCOPE,
        TAKEN, DROPPED, GET_ROOM, SET_ROOM, GET_ACTIONS, PROCESS_ACTIONS;
    }
}
