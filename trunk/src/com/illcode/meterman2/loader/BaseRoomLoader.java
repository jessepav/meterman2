package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.ui.UIConstants;
import org.jdom2.Element;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Loader for system room implementations.
 * <p/>
 * The type of room created can be controlled by the 'type' attribute of the XML element
 * from which the definition is read. Valid values of 'type' and the corresponding Room or
 * RoomImpl classes are:
 * <dl>
 *     <dt>"dark"</dt>
 *     <dd>{@link DarkRoom}</dd>
 * </dl>
 * Otherwise we create an instance of {@link Room} with a {@link BaseRoomImpl} implementation.
 */
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
        Room r;
        switch (defaultString(el.getAttributeValue("type"))) {
        case "dark":
            r = DarkRoom.create(id);
            break;
        default:
            r = Room.create(id);
            break;
        }
        return r;
    }

    public void loadRoomProperties(XBundle bundle, Element el, Room r, GameObjectIdResolver resolver)
    {
        LoaderHelper helper = LoaderHelper.wrap(el);
        loadBasicProperties(bundle, el, r, resolver, helper);  // always load basic properties
        switch (defaultString(el.getAttributeValue("type"))) {  // and then perhaps class-specific properties
        case "dark":
            RoomImpl impl = r.getImpl();
            if (r instanceof DarkRoom)
                loadDarkRoomProperties(bundle, el, (DarkRoom) r, resolver, helper);
            break;
        }
    }

    private void loadBasicProperties(XBundle bundle, Element el, Room r, GameObjectIdResolver resolver,
                                     LoaderHelper helper) {
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

        // connections
        final Element exits = el.getChild("exits");
        if (exits != null) {
            for (Element exit : exits.getChildren("exit")) {
                final Room room = resolver.getRoom(exit.getAttributeValue("room"));
                final int pos = UIConstants.buttonTextToPosition(exit.getAttributeValue("pos"));
                final String exitLabel = exit.getAttributeValue("label");
                if (room != null && pos != -1) {
                    r.setExit(pos, room);
                    r.setExitLabel(pos, exitLabel);
                }
            }
        }
    }

    private void loadDarkRoomProperties(XBundle bundle, Element el, DarkRoom r,
                                        GameObjectIdResolver resolver, LoaderHelper helper) {
        r.setDarkName(helper.getValue("darkName"));
        r.setDarkExitName(helper.getValue("darkExitName"));
        final Element darkDescription = el.getChild("darkDescription");
        if (darkDescription != null)
            r.setDarkDescription(bundle.elementTextSource(darkDescription));
    }
}
