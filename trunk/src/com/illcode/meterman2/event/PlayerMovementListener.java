package com.illcode.meterman2.event;

import com.illcode.meterman2.model.Room;

/**
 * A PlayerMovementListener is notified before and after the player moves rooms
 */
public interface PlayerMovementListener extends GameEventHandler
{
    /**
     * Called when a player moves rooms. It may be called twice per player movement, once
     * before the move has actually occurred, and once after, unless another PlayerMovementListener
     * or the room itself interrupts the processing chain.
     * @param fromRoom the room the player is moving, or has moved, from
     * @param toRoom the room the player is moving, or has moved, to
     * @param beforeMove true if this method is being called before the player moves, and
     *                   false if the move has already occurred.
     * @return true if further processing of the player move command should be blocked;
     *         false to let processing continue as usual.
     */
    boolean playerMove(Room fromRoom, Room toRoom, boolean beforeMove);
}
