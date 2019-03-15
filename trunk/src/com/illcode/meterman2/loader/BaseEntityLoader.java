package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.*;
import org.jdom2.Element;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Loader for base entity implementations.
 * <p/>
 * The type of entity created can be controlled by the 'type' attribute of the XML element
 * from which the definition is read. Valid values of 'type' and the corresponding Entity or
 * EntityImpl classes are:
 * <dl>
 *     <dt>"container"</dt>
 *     <dd>{@link Container} + {@link ContainerImpl}</dd>
 *     <dt>"door"</dt>
 *     <dd>{@link DoorImpl}</dd>
 * </dl>
 * Otherwise we create an instance of {@link Entity} with a {@link BaseEntityImpl} implementation.
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
        case "door":
            e = Entity.create(id, new DoorImpl());
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
        loadBasicProperties(bundle, el, e, resolver, helper);  // always load basic properties
        switch (defaultString(el.getAttributeValue("type"))) {  // and then perhaps class-specific properties
        case "container":
            if (e instanceof Container)
                loadContainerProperties(bundle, el, (Container) e, resolver, helper);
            break;
        case "door":
            if (e.getImpl() instanceof DoorImpl)
                loadDoorProperties(bundle, el, (DoorImpl) e.getImpl(), resolver, helper);
            break;
        }

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

    protected void loadDoorProperties(XBundle bundle, Element el, DoorImpl doorImpl, GameObjectIdResolver resolver,
                                      LoaderHelper helper) {

    }

}
