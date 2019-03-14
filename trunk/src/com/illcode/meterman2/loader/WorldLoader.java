package com.illcode.meterman2.loader;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.BundleGroup;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.GameObjectIdResolver;
import com.illcode.meterman2.model.Room;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * Loads our world from a bundle group.
 */
public final class WorldLoader implements GameObjectIdResolver
{
    private BundleGroup group;
    private Map<String,LoadInfo<Entity,EntityLoader>> entityIdMap;
    private Map<String,LoadInfo<Room,RoomLoader>> roomIdMap;

    /**
     * Create a new world loader.
     * @param group bundle group where we find our elements
     */
    public WorldLoader(BundleGroup group) {
        this.group = group;
        entityIdMap = new HashMap<>(100);
        roomIdMap = new HashMap<>(40);
    }

    /**
     * Create an entity from a bundle element with a given ID and add it to the entityIdMap.
     * @param id element (and thus entity) ID
     */
    private void createEntity(String id) {
        Pair<Element,XBundle> pair = group.getElementAndBundle(id);
        if (pair == null)
            return;
        Element el = pair.getLeft();
        XBundle bundle = pair.getRight();
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
        entityIdMap.put(id, LoadInfo.of(loader.createEntity(bundle, el, id), loader, el, bundle));
    }

    /**
     * Create a room from a bundle element with a given ID and add it to the roomIdMap.
     * @param id element (and thus room) ID
     */
    private void createRoom(String id) {
        Pair<Element,XBundle> pair = group.getElementAndBundle(id);
        if (pair == null)
            return;
        Element el = pair.getLeft();
        XBundle bundle = pair.getRight();
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
        roomIdMap.put(id, LoadInfo.of(loader.createRoom(bundle, el, id), loader, el, bundle));
    }

    /**
     * Loads the properties of an entity using the same loader that created it.
     * This should be called only after all entities and rooms have been created.
     * @param id entity ID.
     */
    private void loadEntityProperties(String id) {
        LoadInfo<Entity,EntityLoader> eli = entityIdMap.get(id);
        eli.loader.loadEntityProperties(eli.bundle, eli.element, eli.gameObject, this);
    }

    /**
     * Loads the properties of an entity using the same loader that created it.
     * This should be called only after all entities and rooms have been created.
     * @param id entity ID.
     */
    private void loadRoomProperties(String id) {
        LoadInfo<Room,RoomLoader> rli = roomIdMap.get(id);
        rli.loader.loadRoomProperties(rli.bundle, rli.element, rli.gameObject, this);
    }

    //region -- Implement GameObjectIdResolver --
    public Entity getEntity(String id) {
        return entityIdMap.get(id).gameObject;
    }

    public Room getRoom(String id) {
        return roomIdMap.get(id).gameObject;
    }
    //endregion

    private static final class LoadInfo<T,S>
    {
        T gameObject;
        S loader;
        Element element;
        XBundle bundle;

        private LoadInfo(T gameObject, S loader, Element element, XBundle bundle) {
            this.gameObject = gameObject;
            this.loader = loader;
            this.element = element;
            this.bundle = bundle;
        }

        static <T,S> LoadInfo<T,S> of(T gameObject, S loader, Element element, XBundle bundle) {
            return new LoadInfo<>(gameObject, loader, element, bundle);
        }
    }
}
