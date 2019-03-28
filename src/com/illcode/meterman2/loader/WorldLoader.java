package com.illcode.meterman2.loader;

import com.illcode.meterman2.*;
import com.illcode.meterman2.bundle.BundleGroup;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.illcode.meterman2.MMLogging.logger;
import static com.illcode.meterman2.SystemAttributes.TAKEABLE;
import static com.illcode.meterman2.SystemAttributes.EQUIPPABLE;

/**
 * Loads our world from a bundle group.
 */
public final class WorldLoader implements GameObjectResolver
{
    private BundleGroup group;
    private Map<String,LoadInfo<Entity,EntityLoader>> entityLoadInfoMap;
    private Map<String,LoadInfo<Room,RoomLoader>> roomLoadInfoMap;
    private Map<String, TopicMap> topicMaps;
    private Player player;
    private Room startingRoom;

    /**
     * Create a new world loader.
     * @param group bundle group where we find our elements
     */
    public WorldLoader(BundleGroup group) {
        this.group = group;
        entityLoadInfoMap = new HashMap<>(100);
        roomLoadInfoMap = new HashMap<>(40);
        topicMaps = new HashMap<>();
    }

    /**
     * Load all the <em>entity</em> and <em>room</em> elements found in the bundle group.
     * @param processContainment true if entities should be put into their specified containers
     * and rooms should have their exits connected.
     */
    public void loadAllGameObjects(boolean processContainment) {
        // Create entities and rooms, populating the entityLoadInfoMap and roomLoadInfoMap
        for (Pair<Element,XBundle> pair : group.getElementsAndBundles("entity"))
            createEntity(pair.getLeft(), pair.getRight());
        for (Pair<Element,XBundle> pair : group.getElementsAndBundles("room"))
            createRoom(pair.getLeft(), pair.getRight());

        // and load their properties.
        for (LoadInfo<Entity,EntityLoader> eli : entityLoadInfoMap.values())
            eli.loader.loadEntityProperties(eli.bundle, eli.element, eli.gameObject, this, processContainment);
        for (LoadInfo<Room,RoomLoader> rli : roomLoadInfoMap.values())
            rli.loader.loadRoomProperties(rli.bundle, rli.element, rli.gameObject, this, processContainment);

        // Load the player.
        player = new Player();
        if (processContainment) {
            Element playerEl = group.getElement("player");
            if (playerEl != null) {
                LoaderHelper helper = LoaderHelper.wrap(playerEl);
                startingRoom = getRoom(helper.getValue("inRoom"));
                Element inventory = playerEl.getChild("inventory");
                if (inventory != null) {
                    List<Element> items = inventory.getChildren("item");
                    for (Element item : items) {
                        final Entity itemEntity = getEntity(item.getTextTrim());
                        if (itemEntity == null)
                            continue;
                        final AttributeSet attr = itemEntity.getAttributes();
                        if (attr.get(TAKEABLE)) {
                            GameUtils.putInContainer(itemEntity, player);
                            if (Utils.parseBoolean(item.getAttributeValue("equipped")) && attr.get(EQUIPPABLE))
                                player.equipEntity(itemEntity);
                        }
                    }
                }
            }
        }
    }

    /**
     * Create an entity from an XML definition and add it to the entityLoadInfoMap.
     * @param el element
     * @param bundle bundle where the element was found
     */
    private void createEntity(Element el, XBundle bundle) {
        String id = el.getAttributeValue("id");
        LoaderHelper helper = LoaderHelper.wrap(el);
        String snippet = helper.getValue("loader");
        EntityLoader loader;
        if (snippet == null) {
            loader = BaseEntityLoader.getInstance();
        } else {
            try {
                loader = (EntityLoader) Meterman2.script.evalScript(snippet);
            } catch (ClassCastException ex) {
                logger.warning("ClassCastException in WorldLoader for id '" + id + "'");
                return;
            }
            if (loader == null) {
                logger.warning("Null loader in WorldLoader for id '" + id + "'");
                return;
            }
        }
        entityLoadInfoMap.put(id, new LoadInfo<>(loader.createEntity(bundle, el, id), loader, el, bundle));
    }

    /**
     * Create a room from an XML definition and add it to the roomLoadInfoMap.
     * @param el element
     * @param bundle bundle where the element was found
     */
    private void createRoom(Element el, XBundle bundle) {
        String id = el.getAttributeValue("id");
        LoaderHelper helper = LoaderHelper.wrap(el);
        String snippet = helper.getValue("loader");
        RoomLoader loader;
        if (snippet == null) {
            loader = BaseRoomLoader.getInstance();
        } else {
            try {
                loader = (RoomLoader) Meterman2.script.evalScript(snippet);
            } catch (ClassCastException ex) {
                logger.warning("ClassCastException in WorldLoader for id '" + id + "'");
                return;
            }
            if (loader == null) {
                logger.warning("Null loader in WorldLoader for id '" + id + "'");
                return;
            }
        }
        roomLoadInfoMap.put(id, new LoadInfo<>(loader.createRoom(bundle, el, id), loader, el, bundle));
    }

    /**
     * Reload an entity's properties from its definition.
     * @param id entity ID
     * @return the entity reloaded on success, null on failure
     */
    public Entity reloadEntity(String id) {
        LoadInfo<Entity,EntityLoader> eli = entityLoadInfoMap.get(id);
        if (eli == null)
            return null;
        if (!eli.bundle.reloadElement(id))
            return null;
        Element e = eli.bundle.getElement(id);
        if (e == null)
            return null;
        eli.element = e;
        eli.loader.loadEntityProperties(eli.bundle, eli.element, eli.gameObject, this, false);
        return eli.gameObject;
    }

    /**
     * Reload a rooms's properties from its definition.
     * @param id room ID
     * @return the Room reloaded on success, null on failure
     */
    public Room reloadRoom(String id) {
        LoadInfo<Room,RoomLoader> rli = roomLoadInfoMap.get(id);
        if (rli == null)
            return null;
        if (!rli.bundle.reloadElement(id))
            return null;
        Element e = rli.bundle.getElement(id);
        if (e == null)
            return null;
        rli.element = e;
        rli.loader.loadRoomProperties(rli.bundle, rli.element, rli.gameObject, this, false);
        return rli.gameObject;
    }

    /**
     * Reloads a top-level topic map from its definition. The topic map must have been previously loaded
     * for this method to succeed.
     * @param id topic map element ID
     * @return the TopicMap reloaded on success, null on failure.
     */
    public TopicMap reloadTopicMap(String id) {
        TopicMap tm = topicMaps.get(id);
        if (tm == null)
            return null;
        final Pair<Element,XBundle> pair = group.getElementAndBundle(id);
        if (pair == null)
            return null;
        final Element el = pair.getLeft();
        final XBundle b = pair.getRight();
        tm.loadFrom(el, b);
        return tm;
    }

    // Methods to retrieve the world.

    public Player getPlayer() {
        return player;
    }

    public Room getStartingRoom() {
        return startingRoom;
    }

    public Map<String,Entity> getEntityIdMap() {
        Map<String, Entity> entityIdMap = Utils.createSizedHashMap(entityLoadInfoMap);
        for (Map.Entry<String,LoadInfo<Entity,EntityLoader>> entry : entityLoadInfoMap.entrySet()) {
            String id = entry.getKey();
            LoadInfo<Entity,EntityLoader> loadInfo = entry.getValue();
            entityIdMap.put(id, loadInfo.gameObject);
        }
        return entityIdMap;
    }

    public Map<String,Room> getRoomIdMap() {
        Map<String, Room> roomIdMap = new HashMap<>((int) (roomLoadInfoMap.size() * 1.4f), 0.75f);
        for (Map.Entry<String,LoadInfo<Room,RoomLoader>> entry : roomLoadInfoMap.entrySet()) {
            String id = entry.getKey();
            LoadInfo<Room,RoomLoader> loadInfo = entry.getValue();
            roomIdMap.put(id, loadInfo.gameObject);
        }
        return roomIdMap;
    }

    //region -- Implement GameObjectResolver --
    public Entity getEntity(String id) {
        final LoadInfo<Entity,EntityLoader> eli = entityLoadInfoMap.get(id);
        if (eli != null)
            return eli.gameObject;
        else
            return null;
    }

    public Room getRoom(String id) {
        final LoadInfo<Room,RoomLoader> rli = roomLoadInfoMap.get(id);
        if (rli != null)
            return rli.gameObject;
        else
            return null;
    }

    public TopicMap getTopicMap(String id) {
        TopicMap tm = topicMaps.get(id);
        if (tm == null) {
            final Pair<Element,XBundle> pair = group.getElementAndBundle(id);
            if (pair != null) {
                final Element el = pair.getLeft();
                final XBundle b = pair.getRight();
                tm = new TopicMap();
                tm.loadFrom(el, b);
                topicMaps.put(id, tm);
            }
        }
        return tm;
    }
    //endregion

    private static final class LoadInfo<T,S>
    {
        T gameObject;
        S loader;
        Element element;
        XBundle bundle;

        LoadInfo(T gameObject, S loader, Element element, XBundle bundle) {
            this.gameObject = gameObject;
            this.loader = loader;
            this.element = element;
            this.bundle = bundle;
        }
    }
}
