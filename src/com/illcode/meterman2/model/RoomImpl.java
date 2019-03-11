package com.illcode.meterman2.model;

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

    /** See {@link Room#entered(Room)} */
    void entered(Room r, Room fromRoom);

    /** See {@link Room#exiting(Room)} */
    boolean exiting(Room r, Room toRoom);

    enum RoomMethod
    {
        GET_DESCRIPTION("getDescription"), ENTERED("entered"), EXITING("exiting");

        private final String methodName;

        RoomMethod(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }

    }
}
