package com.illcode.meterman2.model;

/**
 * Used to indicate which methods of an Entity will be delegated.
 */
public enum EntityImplMethods
{
    GET_ATTRIBUTES, GET_NAME, GET_DESCRIPTION, LOOK_IN_ROOM, ENTER_SCOPE, EXITING_SCOPE,
    TAKEN, DROPPED, GET_ROOM, SET_ROOM, GET_ACTIONS, PROCESS_ACTIONS, SAVE_STATE, RESTORE_STATE;
}
