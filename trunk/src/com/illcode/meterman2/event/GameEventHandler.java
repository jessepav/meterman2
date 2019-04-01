package com.illcode.meterman2.event;

import com.illcode.meterman2.model.Game;

/**
 * The super-interface for all event listeners, processors, handlers etc.
 */
public interface GameEventHandler
{
    /**
     * Return the ID of this GameEventHandler. The ID is used to persist the active listeners when a game is
     * saved and subsequently loaded, without needing to serialize the actual handler objects. If the ID
     * starts with a '#', then the corresponding handler will not be persisted.
     */
    String getHandlerId();
}
