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
    /** See {@link Room#getName()} */
    String getName(Room r);

    /** See {@link Room#getExitName()} */
    String getExitName(Room r);

    /** See {@link Room#getDescription()} */
    String getDescription(Room r);

    /** See {@link Room#getExit(int)} */
    Room getExit(Room r, int direction);

    /** See {@link Room#getExitLabel(int)} */
    String getExitLabel(Room r, int direction);

    /** See {@link Room#entered(Room)} */
    void entered(Room r, Room fromRoom);

    /** See {@link Room#exiting(Room)} */
    boolean exiting(Room r, Room toRoom);

    enum Methods
    {
        GET_NAME, GET_EXIT_NAME, GET_DESCRIPTION, GET_EXIT, GET_EXIT_LABEL,
        ENTERED, EXITING;
    }
}
