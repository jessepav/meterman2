package com.illcode.meterman2.loader;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.model.ScriptedRoomImpl;
import org.jdom2.Element;

import java.util.List;

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
    }
}
