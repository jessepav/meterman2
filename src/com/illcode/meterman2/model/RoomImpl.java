package com.illcode.meterman2.model;

import com.illcode.meterman2.text.TextSource;

/**
 * Defines an implementation for room operation, used for both room implementation instances
 * and delegates.
 * <p/>
 * The methods parallel those of {@link Room}, with the addition of a parameter indicating
 * the room whose operations we should implement.
 */

public interface RoomImpl
{
    /** See {@link Room#getDescription()} */
    String getDescription(Room r);

    /** Sets the description. Some implementations, for instance scripted implementations,
     *  may ignore calls to this method. */
    void setDescription(TextSource description);

    /** See {@link Room#entered(Room)} */
    void entered(Room r, Room fromRoom);

    /** See {@link Room#exiting(Room)} */
    boolean exiting(Room r, Room toRoom);

    /** See {@link Room#eachTurn()} */
    void eachTurn(Room r);

    /** See {@link Room#getState()} */
    Object getState(Room r);

    /** See {@link Room#restoreState(Object)} */
    void restoreState(Room r, Object state);

    enum RoomMethod
    {
        GET_DESCRIPTION("getDescription"), ENTERED("entered"), EXITING("exiting"),
        EACH_TURN("eachTurn"), GET_STATE("getState"), RESTORE_STATE("restoreState");

        private final String methodName;

        RoomMethod(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }

    }
}
