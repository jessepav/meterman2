package com.illcode.meterman2.loader;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.SystemAttributes;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.ui.UIConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static com.illcode.meterman2.MMLogging.logger;

/**
 * Loader for system entity implementations.
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

    public void loadEntityProperties(XBundle bundle, Element el, Entity e,
                                     GameObjectIdResolver resolver, boolean processContainment)
    {
        LoaderHelper helper = LoaderHelper.wrap(el);
        loadBasicProperties(bundle, el, e, resolver, processContainment, helper);  // always load basic properties
        switch (defaultString(el.getAttributeValue("type"))) {  // and then perhaps class-specific properties
        case "container":
            if (e.getImpl() instanceof ContainerImpl)
                loadContainerProperties(bundle, el, (ContainerImpl) e.getImpl(), resolver, helper);
            break;
        case "door":
            if (e.getImpl() instanceof DoorImpl) {
                final DoorImpl doorImpl = (DoorImpl) e.getImpl();
                if (loadDoorProperties(bundle, el, doorImpl, resolver, helper)) {
                    doorImpl.updateRoomConnections(e);  // connect/disconnect rooms
                    if (processContainment) {
                        // Put the door into both its rooms
                        e.setContainer(null); // it is a strange creation, nowhere...
                        Pair<Room,Room> rooms = doorImpl.getRooms();
                        rooms.getLeft().addEntity(e); //...and yet manifold.
                        rooms.getRight().addEntity(e);
                    }
                    final AttributeSet attr = e.getAttributes();
                    attr.clear(SystemAttributes.TAKEABLE); // doors shall not move!
                    attr.clear(SystemAttributes.MOVEABLE);
                } else {
                    logger.warning("Error in loadDoorProperties for ID " + e.getId());
                }
            }
            break;
        }

    }

    protected void loadBasicProperties(XBundle bundle, Element el, Entity e, GameObjectIdResolver resolver,
                                       boolean processContainment, LoaderHelper helper) {
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

        if (processContainment) {
            final String room = helper.getValue("inRoom");
            if (room != null) {
                Room r = resolver.getRoom(room);
                if (r != null) {
                    e.setContainer(r);
                    r.addEntity(e);
                }
            } else {
                final String container = helper.getValue("inContainer");
                if (container != null) {
                    Entity e2 = resolver.getEntity(container);
                    if (e2 instanceof EntityContainer) {
                        EntityContainer c = (EntityContainer) e2;
                        e.setContainer(c);
                        c.addEntity(e);
                    }
                }
            }
        }
    }

    protected void loadContainerProperties(XBundle bundle, Element el, ContainerImpl containerImpl, GameObjectIdResolver resolver,
                                           LoaderHelper helper) {
        containerImpl.setInPrep(helper.getValue("inPrep"));
        containerImpl.setOutPrep(helper.getValue("outPrep"));
        containerImpl.setKey(resolver.getEntity(helper.getValue("key")));
    }

    // Returns true if the door was loaded successfully.
    protected boolean loadDoorProperties(XBundle bundle, Element el, DoorImpl doorImpl, GameObjectIdResolver resolver,
                                         LoaderHelper helper) {
        final Element connects = el.getChild("connects");
        if (connects == null)
            return false;
        final Room room1 = resolver.getRoom(connects.getAttributeValue("room1"));
        final Room room2 = resolver.getRoom(connects.getAttributeValue("room2"));
        final int pos1 = UIConstants.buttonTextToPosition(connects.getAttributeValue("pos1"));
        final int pos2 = UIConstants.buttonTextToPosition(connects.getAttributeValue("pos2"));
        if (room1 == null || room2 == null || pos1 == -1 || pos2 == -1)
            return false;
        doorImpl.setRooms(room1, room2);
        doorImpl.setPositions(pos1, pos2);
        doorImpl.setClosedExitLabel(helper.getValue("closedExitLabel"));
        doorImpl.setKey(resolver.getEntity(helper.getValue("key")));
        return true;
    }

}
