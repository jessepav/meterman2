package com.illcode.meterman2.event;

import com.illcode.meterman2.model.Room;

/**
 * Interface for listeners that want to be notified when a look command has been invoked.
 */
public interface LookListener extends GameEventHandler
{
    /**
     * Called when a look command is issued. While the implementation is free to do anything, generally it
     * will call {@link com.illcode.meterman2.GameManager#queueLookText} to add text to the room description
     * printed.
     * @param currentRoom the room where the player is looking. This parameter will also be bound to
     * <tt>"currentRoom"</tt> in the template and script namespaces.
     */
    void lookInRoom(Room currentRoom);
}
