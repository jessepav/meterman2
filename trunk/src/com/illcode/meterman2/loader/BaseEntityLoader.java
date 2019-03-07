package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import org.jdom2.Element;

/**
 * Loader for base entity implementations.
 */
public class BaseEntityLoader implements EntityLoader
{
    private static BaseEntityLoader instance;

    private BaseEntityLoader() {
        // empty
    }

    /**
     * Retrieve the instance of this loader. Not thread safe!
     */
    public static BaseEntityLoader getInstance() {
        if (instance == null)
            instance = new BaseEntityLoader();
        return instance;
    }

    public Entity loadFromXml(XBundle bundle, Element el, String id) {
        Entity e = Entity.create(id);
        loadPropertiesFromXml(bundle, el, e);
        return e;
    }

    /**
     * Load values for the various entity properties from an XML element.
     * Subclasses of BaseEntityLoader can chain up to this method to load standard entity
     * properties before loading subclass-specific properties.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the entity defintion
     * @param e the entity whose properties will be set from values in the XML element.
     */
    protected void loadPropertiesFromXml(XBundle bundle, Element el, Entity e) {
        // TODO: BaseEntityLoader.loadPropertiesFromXml()
    }
}
