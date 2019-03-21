package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.GameObjectIdResolver;
import org.jdom2.Element;

/**
 * An object that can instantiate and load entities based on an XML definition.
 */
public interface EntityLoader
{
    /**
     * Called by the game system to construct and return an Entity (or subclass)
     * based on an XML definition. It does not load properties into the entity --
     * that is performed by {@link #loadEntityProperties}.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the entity defintion
     * @param id ID for the new entity
     * @return a new entity
     */
    Entity createEntity(XBundle bundle, Element el, String id);

    /**
     * Load values for the various entity properties from an XML element.
     * Subclasses of EntityLoader implementations can chain up to their parent's method to load standard entity
     * properties before loading subclass-specific properties. The implementation of hot-reloading
     * will likely end up using this method as well.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the entity defintion
     * @param e the entity whose properties will be set from values in the XML element.
     * @param resolver used to resolve references in the entity, for instance if a door refers
     * @param processContainment true if the containment of the entity should be processed
     */
    void loadEntityProperties(XBundle bundle, Element el, Entity e, GameObjectIdResolver resolver, boolean processContainment);
}
