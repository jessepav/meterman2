package com.illcode.meterman2.event;

/**
 * A TurnListener is notified at the transition point between turns.
 */
public interface TurnListener extends GameEventHandler
{
    /** Called at the transition point between turns. */
    void turn();
}
