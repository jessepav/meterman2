package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import org.jdom2.Element;

public interface EntityLoader
{
    /**
     * Called by the game system to construct and return an Entity (or subclass) with
     * properties defined in an XBundle element.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the entity defintion
     * @param id ID for the new entity
     * @return a new entity
     */
    Entity loadFromXml(XBundle bundle, Element el, String id);

    /**
     * Load values for the various entity properties from an XML element.
     * Subclasses of EntityLoader implementations can chain up to their parent's method to load standard entity
     * properties before loading subclass-specific properties. The implementation of hot-reloading
     * will likely end up using this method as well.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the entity defintion
     * @param e the entity whose properties will be set from values in the XML element.
     */
    void loadPropertiesFromXml(XBundle bundle, Element el, Entity e);
}
