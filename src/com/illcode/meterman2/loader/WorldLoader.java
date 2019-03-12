package com.illcode.meterman2.loader;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;
import org.jdom2.Element;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * Loads our world from a bundle.
 */
public final class WorldLoader
{
    private XBundle bundle;

    public WorldLoader(XBundle bundle) {
        this.bundle = bundle;
    }

    /** Loads an entity from a bundle element with a given ID.
     * @param id element (and thus entity) ID
     * @return entity thus loaded, or null if error.
     */
    public Entity loadEntity(String id) {
        Element el = bundle.getElement("id");
        if (el == null)
            return null;
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
                return null;
            }
            if (loader == null) {
                logger.warning("Null loader in WorldLoader for id '" + id + "'");
                return null;
            }
        }
        return loader.loadFromXml(bundle, el, id);
    }

    /** Loads a room from a bundle element with a given ID.
     * @param id element (and thus room) ID
     * @return room thus loaded, or null if error.
     */
    public Room loadRoom(String id) {
        Element el = bundle.getElement("id");
        if (el == null)
            return null;
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
                return null;
            }
            if (loader == null) {
                logger.warning("Null loader in WorldLoader for id '" + id + "'");
                return null;
            }
        }
        return loader.loadFromXml(bundle, el, id);
    }
}
