package com.illcode.meterman2.event;

/**
 * A TurnListener is notified at the transition point between turns.
 */
public interface TurnListener extends GameEventHandler
{
    /** Called at the transition point between turns. If this listener is registered at the
     *  start of a new game, it will be called once after the initial "Look" command. */
    void turn();
}
