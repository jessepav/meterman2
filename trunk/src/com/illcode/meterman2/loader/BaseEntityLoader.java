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

    public void loadPropertiesFromXml(XBundle bundle, Element el, Entity e) {
        // TODO: BaseEntityLoader.loadPropertiesFromXml()
    }
}
