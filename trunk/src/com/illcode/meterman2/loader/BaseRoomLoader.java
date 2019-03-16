package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.GameObjectIdResolver;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.model.ScriptedRoomImpl;
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

    public Room createRoom(XBundle bundle, Element el, String id) {
        return Room.create(id);
    }

    public void loadRoomProperties(XBundle bundle, Element el, Room r, GameObjectIdResolver resolver)
    {
        LoaderHelper helper = LoaderHelper.wrap(el);

        // Text properties
        r.setName(helper.getValue("name"));
        r.setExitName(helper.getValue("exitName"));
        final Element description = el.getChild("description");
        if (description != null)
            r.getImpl().setDescription(bundle.elementTextSource(description));

        // Attributes
        helper.loadAttributes("attributes", r.getAttributes());

        // Scripted delegate
        final Element script = el.getChild("script");
        if (script != null) {
            ScriptedRoomImpl scriptedImpl = new ScriptedRoomImpl(r.getId(), script.getTextTrim());
            r.setDelegate(scriptedImpl, scriptedImpl.getScriptedRoomMethods());
        }

        // TODO: room connections
    }
}
