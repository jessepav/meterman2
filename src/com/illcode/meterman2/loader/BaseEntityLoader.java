package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Container;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.GameObjectIdResolver;
import com.illcode.meterman2.model.ScriptedEntityImpl;
import org.jdom2.Element;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Loader for base entity implementations.
 * <p/>
 * The type of entity created can be controlled by the 'type' attribute of the XML element
 * from which the definition is read. Valid values of 'type' and the corresponding Entity class are:
 * <dl>
 *     <dt>"container"</dt>
 *     <dd>{@link Container}</dd>
 * </dl>
 * Otherwise we create an instance of the basic {@link Entity}.
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

    public Entity createEntity(XBundle bundle, Element el, String id) {
        Entity e;
        switch (defaultString(el.getAttributeValue("type"))) {
        case "container":
            e = Container.create(id);
            break;
        default:
            e = Entity.create(id);
            break;
        }
        return e;
    }

    public void loadEntityProperties(XBundle bundle, Element el, Entity e, GameObjectIdResolver resolver)
    {
        LoaderHelper helper = LoaderHelper.wrap(el);
        loadBasicProperties(bundle, el, e, resolver, helper);
        if (e instanceof Container)
            loadContainerProperties(bundle, el, (Container) e, resolver, helper);
    }

    protected void loadBasicProperties(XBundle bundle, Element el, Entity e, GameObjectIdResolver resolver,
                                       LoaderHelper helper) {
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

    protected void loadContainerProperties(XBundle bundle, Element el, Container c, GameObjectIdResolver resolver,
                                           LoaderHelper helper) {
        c.setInPrep(helper.getValue("inPrep"));
        c.setOutPrep(helper.getValue("outPrep"));
    }

}
