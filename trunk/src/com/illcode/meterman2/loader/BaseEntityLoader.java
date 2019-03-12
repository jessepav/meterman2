package com.illcode.meterman2.loader;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import org.jdom2.Element;

import java.util.List;

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
        LoaderHelper we = LoaderHelper.wrap(el);
        e.setName(we.getValue("name"));
        e.setIndefiniteArticle(we.getValue("indefiniteArticle"));
        final Element description = el.getChild("description");
        if (description != null)
            e.getImpl().setDescription(bundle.elementTextSource(description));
        AttributeSet entityAttr = e.getAttributes();
        List<String> attributeNames = we.getListValue("attributes");
        for (String name : attributeNames) {
            int attrNum = Meterman2.attributes.attributeForName(name);
            if (attrNum != -1)
                entityAttr.set(attrNum);
        }
    }
}
