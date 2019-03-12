package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.ScriptedEntityImpl;
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
        LoaderHelper helper = LoaderHelper.wrap(el);

        // Text properties
        e.setName(helper.getValue("name"));
        e.setIndefiniteArticle(helper.getValue("indefiniteArticle"));
        final Element description = el.getChild("description");
        if (description != null)
            e.getImpl().setDescription(bundle.elementTextSource(description));

        // Attributes
        helper.loadAttributes("attributes", e.getAttributes());

        // Scripted delegate
        final Element script = el.getChild("script");
        if (script != null) {
            ScriptedEntityImpl scriptedImpl = new ScriptedEntityImpl(e.getId(), script.getTextTrim());
            e.setDelegate(scriptedImpl, scriptedImpl.getScriptedEntityMethods());
        }
    }
}
