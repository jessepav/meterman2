package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import org.jdom2.Element;

public interface EntityLoader
{
    /**
     * Called by the game system to construct and return an Entity (or subclass) with
     * properties defined in an XBundle.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the entity defintion
     * @param id ID for the new entity
     * @return a new entity
     */
    Entity loadFromXml(XBundle bundle, Element el, String id);
}
