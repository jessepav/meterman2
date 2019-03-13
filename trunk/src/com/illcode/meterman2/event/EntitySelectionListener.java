package com.illcode.meterman2.event;

import com.illcode.meterman2.model.Entity;

/**
 * A listener that will be notified when an entity is selected in the UI.
 * <p/>
 * This can be used, for example, to set entity images in the UI, or to manage puzzles
 * that involve selecting different entities in a certain order.
 */
public interface EntitySelectionListener extends GameEventHandler
{
    /**
     * Called when an entity is selected.
     * </p>
     * Note that it is called <em>after</em> the UI is updated as normal for that entity,
     * so that the listener can modify it if desired.
     * @param e selected entity
     * @return true if other selection listeners should be skipped, false to continue processing.
     */
    boolean entitySelected(Entity e);
}
