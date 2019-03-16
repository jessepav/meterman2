package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.ui.UIConstants;
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
            if (e.getImpl() instanceof ContainerImpl)
                loadContainerProperties(bundle, el, (ContainerImpl) e.getImpl(), resolver, helper);
            break;
        case "door":
            if (e.getImpl() instanceof DoorImpl) {
                final DoorImpl doorImpl = (DoorImpl) e.getImpl();
                loadDoorProperties(bundle, el, doorImpl, resolver, helper);
                doorImpl.updateRoomConnections(e);  // (dis)connect rooms
            }
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

    protected void loadContainerProperties(XBundle bundle, Element el, ContainerImpl containerImpl, GameObjectIdResolver resolver,
                                           LoaderHelper helper) {
        containerImpl.setInPrep(helper.getValue("inPrep"));
        containerImpl.setOutPrep(helper.getValue("outPrep"));
        containerImpl.setKey(resolver.getEntity(helper.getValue("key")));
    }

    protected void loadDoorProperties(XBundle bundle, Element el, DoorImpl doorImpl, GameObjectIdResolver resolver,
                                      LoaderHelper helper) {
        readRooms: {
            final Element roomsEl = el.getChild("rooms");
            if (roomsEl == null)
                break readRooms;
            final Room[] rooms = new Room[2];
            final int[] positions = new int[2];
            final String[] exitLabels = new String[2];
            for (int i = 0; i < 2; i++) {
                final Element roomEl = roomsEl.getChild("room" + (i+1));
                if (roomEl == null)
                    break readRooms;
                rooms[i] = resolver.getRoom(roomEl.getAttributeValue("roomId"));
                positions[i] = UIConstants.buttonTextToPosition(roomEl.getAttributeValue("pos"));
                exitLabels[i] = roomEl.getAttributeValue("label");
                if (rooms[i] == null || positions[i] == -1)
                    break readRooms;
            }
            doorImpl.setRooms(rooms[0], rooms[1]);
            doorImpl.setPositions(positions[0], positions[1]);
            doorImpl.setExitLabels(exitLabels[0], exitLabels[1]);
        }
        doorImpl.setClosedExitLabel(helper.getValue("closedExitLabel"));
        doorImpl.setKey(resolver.getEntity(helper.getValue("key")));
    }

}
