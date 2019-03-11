package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Room;
import org.jdom2.Element;

public class BaseRoomLoader implements RoomLoader
{
    private static BaseRoomLoader instance;

    private BaseRoomLoader() {
        // empty
    }

    /**
     * Retrieve the instance of this loader. Not thread safe!
     */
    public static BaseRoomLoader getInstance() {
        if (instance == null)
            instance = new BaseRoomLoader();
        return instance;
    }

    public Room loadFromXml(XBundle bundle, Element el, String id) {
        Room r = Room.create(id);
        loadPropertiesFromXml(bundle, el, r);
        return r;
    }

    public void loadPropertiesFromXml(XBundle bundle, Element el, Room r) {
        // TODO: BaseRoomLoader.loadPropertiesFromXml()
    }
}
