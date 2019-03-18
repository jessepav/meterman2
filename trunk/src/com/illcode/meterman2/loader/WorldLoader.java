package com.illcode.meterman2.loader;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.Utils;
import com.illcode.meterman2.bundle.BundleGroup;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.GameObjectIdResolver;
import com.illcode.meterman2.model.Player;
import com.illcode.meterman2.model.Room;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * Loads our world from a bundle group.
 */
public final class WorldLoader implements GameObjectIdResolver
{
    private BundleGroup group;
    private Map<String,LoadInfo<Entity,EntityLoader>> entityLoadInfoMap;
    private Map<String,LoadInfo<Room,RoomLoader>> roomLoadInfoMap;
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
    }

    public void loadAllGameObjects() {
        // Create entities and rooms, populating the entityLoadInfoMap and roomLoadInfoMap
        for (Pair<Element,XBundle> pair : group.getElementsAndBundles("entity"))
            createEntity(pair.getLeft(), pair.getRight());
        for (Pair<Element,XBundle> pair : group.getElementsAndBundles("room"))
            createRoom(pair.getLeft(), pair.getRight());

        // and load their properties.
        for (LoadInfo<Entity,EntityLoader> eli : entityLoadInfoMap.values())
            eli.loader.loadEntityProperties(eli.bundle, eli.element, eli.gameObject, this);
        for (LoadInfo<Room,RoomLoader> rli : roomLoadInfoMap.values())
            rli.loader.loadRoomProperties(rli.bundle, rli.element, rli.gameObject, this);

        // Load the player.
        player = new Player();
        Element playerEl = group.getElement("player");
        if (playerEl != null) {
            LoaderHelper helper = LoaderHelper.wrap(playerEl);
            startingRoom = getRoom(helper.getValue("location"));
            Element inventory = playerEl.getChild("inventory");
            if (inventory != null) {
                List<Element> items = inventory.getChildren("item");
                for (Element item : items) {
                    final Entity e = getEntity(item.getTextTrim());
                    if (e != null) {
                        player.addEntity(e);
                        e.setContainer(player);
                        if (Utils.parseBoolean(item.getAttributeValue("equipped")))
                            player.equipEntity(e);
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
                loader = (EntityLoader) Meterman2.script.evalGameScript(snippet);
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
                loader = (RoomLoader) Meterman2.script.evalGameScript(snippet);
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

    //region -- Implement GameObjectIdResolver --
    public Entity getEntity(String id) {
        return entityLoadInfoMap.get(id).gameObject;
    }

    public Room getRoom(String id) {
        return roomLoadInfoMap.get(id).gameObject;
    }
    //endregion

    private static final class LoadInfo<T,S>
    {
        final T gameObject;
        final S loader;
        final Element element;
        final XBundle bundle;

        LoadInfo(T gameObject, S loader, Element element, XBundle bundle) {
            this.gameObject = gameObject;
            this.loader = loader;
            this.element = element;
            this.bundle = bundle;
        }
    }
}
