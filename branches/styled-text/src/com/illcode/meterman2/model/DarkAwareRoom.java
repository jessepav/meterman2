package com.illcode.meterman2.model;

import java.util.List;

/**
 * Implemented by rooms that are aware of darkness, and return a different
 * name, description, and contents when dark.
 */
public interface DarkAwareRoom
{
    /** Get the room name to be used when dark. */
    String getDarkName();

    /** Get the room description to be used when dark. */
    String getDarkDescription();

    /** Get the list of entities to be used when dark. */
    List<Entity> getDarkEntities();
}
