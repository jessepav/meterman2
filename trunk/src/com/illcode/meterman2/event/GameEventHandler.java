package com.illcode.meterman2.event;

import com.illcode.meterman2.model.Game;

/**
 * The super-interface for all event listeners, processors, handlers etc.
 */
public interface GameEventHandler
{
    /**
     * If an event handler's ID begins with this prefix, it indicates that the handler is implemented by
     * a Room, so instead of calling {@link Game#getEventHandler(String)} when restoring game state,
     * the system will use the room with an ID derived by stripping this prefix from the handler ID.
     * That Room must implement the appropriate GameEventHandler sub-interface.
     */
    String ROOM_EVENT_HANDLER_PREFIX = "roomId:";

    /**
     * If an event handler's ID begins with this prefix, it indicates that the handler is implemented by
     * an Entity, so instead of calling {@link Game#getEventHandler(String)} when restoring game state,
     * the system will use the entity with an ID derived by stripping this prefix from the handler ID.
     * That Entity must implement the appropriate GameEventHandler sub-interface.
     */
    String ENTITY_EVENT_HANDLER_PREFIX = "entityId:";

    /**
     * Return the ID of this GameEventHandler. The ID is used to persist the active listeners when a game is
     * saved and subsequently loaded, without needing to serialize the actual handler objects.
     */
    String getHandlerId();
}
