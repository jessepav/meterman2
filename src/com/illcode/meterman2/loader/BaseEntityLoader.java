package com.illcode.meterman2.loader;

import com.illcode.meterman2.*;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.ui.UIConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import java.util.*;

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
 *     <dt>"interacting"</dt>
 *     <dd>{@link InteractingEntityImpl}</dd>
 *     <dt>"switchable"</dt>
 *     <dd>{@link SwitchableEntityImpl}</dd>
 *     <dt>"lamp"</dt>
 *     <dd>{@link com.illcode.meterman2.model.LampImpl}</dd>
 *     <dt>"each-turn"</dt>
 *     <dd>{@link com.illcode.meterman2.model.EachTurnEntityImpl}</dd>
 *     <dt>"walker-talker"</dt>
 *     <dd>{@link WalkerTalkerImpl}</dd>
 * </dl>
 * By default we create an instance of {@link Entity} with a {@link BaseEntityImpl} implementation.
 * <p/>
 * <b>Note</b>: this class is <em>not</em> thread-safe!
 */
public class BaseEntityLoader implements EntityLoader
{
    private static BaseEntityLoader instance;

    // These are used to pass parameters down to protected methods.
    protected XBundle bundle;
    protected Element el;
    protected Entity e;
    protected GameObjectResolver resolver;
    protected boolean newGame;
    protected LoaderHelper helper;
    protected Map<String,MMScript.ScriptedMethod> methodMap;
    protected GameObjectProperties objectProps;

    private BaseEntityLoader() {
        methodMap = new HashMap<>();
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
        case "interacting":
            e = Entity.create(id, null);
            e.setImpl(new InteractingEntityImpl(e));
            break;
        case "switchable":
            e = Entity.create(id, new SwitchableEntityImpl());
            break;
        case "lamp":
            e = Entity.create(id, null);
            e.setImpl(new LampImpl(e));
            break;
        case "each-turn":
            e = Entity.create(id, null);
            e.setImpl(new EachTurnEntityImpl(e));
            break;
        case "walker-talker":
            e = Entity.create(id, null);
            e.setImpl(new WalkerTalkerImpl(e));
            break;
        default:
            e = Entity.create(id);
            break;
        }
        return e;
    }

    public void loadEntityProperties(final XBundle bundle, final Element el, final Entity e,
                                     final GameObjectResolver resolver, final boolean newGame)
    {
        this.bundle = bundle;
        this.el = el;
        this.e = e;
        this.resolver = resolver;
        this.newGame = newGame;
        helper = LoaderHelper.wrap(el);
        methodMap.clear();
        objectProps = Meterman2.gm.objectProps();

        loadScriptedMethods();

        loadBasicProperties();  // always load basic properties
        switch (defaultString(el.getAttributeValue("type"))) {  // and then perhaps class-specific properties
        case "container":
            if (e.getImpl() instanceof ContainerImpl)
                loadContainerProperties((ContainerImpl) e.getImpl());
            break;
        case "door":
            if (e.getImpl() instanceof DoorImpl) {
                final DoorImpl doorImpl = (DoorImpl) e.getImpl();
                if (loadDoorProperties(doorImpl)) {
                    if (newGame) {
                        doorImpl.updateRoomConnections(e);  // connect/disconnect rooms
                        // Put the door into both its rooms
                        GameUtils.putInContainer(e, null);  // it is a strange creation, nowhere...
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
        case "interacting":
            if (e.getImpl() instanceof InteractingEntityImpl)
                loadInteractProperties(((InteractingEntityImpl) e.getImpl()).getInteractSupport());
            break;
        case "switchable":
            if (e.getImpl() instanceof SwitchableEntityImpl)
                loadSwitchableEntityProperties((SwitchableEntityImpl) e.getImpl());
            break;
        case "lamp":
            if (e.getImpl() instanceof LampImpl)
                loadLampProperties((LampImpl) e.getImpl());
            break;
        case "each-turn":
            if (e.getImpl() instanceof EachTurnEntityImpl)
                loadEachTurnProperties((EachTurnEntityImpl) e.getImpl());
            break;
        case "walker-talker":
            if (e.getImpl() instanceof WalkerTalkerImpl) {
                WalkerTalkerImpl wt = (WalkerTalkerImpl) e.getImpl();
                loadInteractProperties(wt.getInteractSupport());
                loadEachTurnProperties(wt.getEachTurnEntityImpl());
            }
            break;
        }
        // So we don't accidentally see stale values.
        this.bundle = null;
        this.el = null;
        this.e = null;
        this.resolver = null;
        this.newGame = false;
        helper = null;
        methodMap.clear();
        objectProps = null;
    }

    // Read all 'script' elements and populate the methodMap.
    private void loadScriptedMethods() {
        final List<Element> scripts = el.getChildren("script");
        for (Element script : scripts) {
            final List<MMScript.ScriptedMethod> methods =
                Meterman2.script.getScriptedMethods(e.getId(), bundle.getElementTextTrim(script));
            for (MMScript.ScriptedMethod sm : methods)
                methodMap.put(sm.getName(), sm);
        }
    }

    protected void loadBasicProperties() {
        // Text properties
        e.setName(helper.getValue("name"));
        e.setIndefiniteArticle(helper.getValue("indefiniteArticle"));
        final Element description = el.getChild("description");
        if (description != null)
            e.getImpl().setDescription(bundle.elementTextSource(description));

        // Attributes
        helper.loadAttributes("attributes", e.getAttributes());

        // Set a delegate if appropriate
        if (!methodMap.isEmpty()) {
            final ScriptedEntityImpl scriptedImpl = new ScriptedEntityImpl(e.getId(), methodMap);
            final EnumSet<EntityImpl.EntityMethod> entityMethodSet = scriptedImpl.getScriptedEntityMethods();
            if (!entityMethodSet.isEmpty())
                e.setDelegate(scriptedImpl, entityMethodSet);
        }

        if (newGame) {
            // Containment
            final String room = helper.getValue("inRoom");
            if (room != null) {
                Room r = resolver.getRoom(room);
                if (r != null)
                    GameUtils.putInContainer(e, r);
            } else {
                final String container = helper.getValue("inContainer");
                if (container != null) {
                    Entity c = resolver.getEntity(container);
                    if (c instanceof EntityContainer)
                        GameUtils.putInContainer(e, (EntityContainer) c);
                }
            }
            // Properties
            helper.setProps(e, objectProps);
        }
    }

    protected void loadContainerProperties(ContainerImpl containerImpl) {
        containerImpl.setInPrep(helper.getValue("inPrep"));
        containerImpl.setOutPrep(helper.getValue("outPrep"));
        containerImpl.setKey(resolver.getEntity(helper.getValue("key")));
        containerImpl.setCapacity(Utils.parseInt(helper.getValue("capacity"), ContainerImpl.DEFAULT_CAPACITY));
    }

    // Returns true if the door was loaded successfully.
    protected boolean loadDoorProperties(DoorImpl doorImpl) {
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

    protected void loadInteractProperties(final InteractSupport interactSupport) {
        final Element topicmapEl = el.getChild("topicmap");
        TopicMap tm = null;
        if (topicmapEl != null) {
            final String topicmapRef = topicmapEl.getAttributeValue("topicmapRef");
            if (topicmapRef != null) {
                tm = resolver.getTopicMap(topicmapRef);
            } else {
                tm = new TopicMap();
                tm.loadFrom(topicmapEl, bundle);
            }
        }
        interactSupport.setTopicMap(tm);
        interactSupport.clearTopics();
        for (String topicId : helper.getListValue("topics"))
            interactSupport.addTopic(topicId);
        interactSupport.setInteractActionText(helper.getValue("interactActionText"));
        interactSupport.setExitTopicId(helper.getValue("exitTopic"));
        interactSupport.setScriptedMethods(methodMap);
        final Element promptMessage = el.getChild("promptMessage");
        if (promptMessage != null)
            interactSupport.setPromptMessage(bundle.elementTextSource(promptMessage));
        final Element noTopicsMessage = el.getChild("noTopicsMessage");
        if (noTopicsMessage != null)
            interactSupport.setNoTopicsMessage(bundle.elementTextSource(noTopicsMessage));
    }

    protected void loadSwitchableEntityProperties(SwitchableEntityImpl impl) {
        impl.setScriptedMethods(methodMap);
    }

    protected void loadLampProperties(LampImpl lamp) {
        lamp.setBurnsFuel(Utils.parseBoolean(el.getAttributeValue("burnsFuel")));
        lamp.setFuelRemaining(Utils.parseInt(el.getAttributeValue("fuelRemaining"), 0));
        lamp.setLowFuelAmount(Utils.parseInt(el.getAttributeValue("lowFuelAmount"), 0));
        lamp.setOnText(el.getAttributeValue("onText"));
        lamp.setOffText(el.getAttributeValue("offText"));
        lamp.setLightActionName(el.getAttributeValue("lightAction"));
        lamp.setDouseActionName(el.getAttributeValue("douseAction"));
        lamp.setBaseName(e.getName());
    }

    protected void loadEachTurnProperties(EachTurnEntityImpl impl) {
        impl.setScriptedMethods(methodMap);
    }
}
