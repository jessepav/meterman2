package com.illcode.meterman2.loader;

import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.*;
import com.illcode.meterman2.ui.UIConstants;
import org.jdom2.Element;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * <p/>
 * <b>Note</b>: this class is <em>not</em> thread-safe!
 */
public class BaseRoomLoader implements RoomLoader
{
    private static BaseRoomLoader instance;

    // These are used to pass parameters down to protected methods.
    protected XBundle bundle;
    protected Element el;
    protected Room r;
    protected GameObjectResolver resolver;
    protected boolean newGame;
    protected LoaderHelper helper;
    protected Map<String,MMScript.ScriptedMethod> methodMap;
    protected GameObjectProperties objectProps;

    private BaseRoomLoader() {
        methodMap = new HashMap<>();
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

    public void loadRoomProperties(XBundle bundle, Element el, Room r, GameObjectResolver resolver,
                                   boolean newGame)
    {
        this.bundle = bundle;
        this.el = el;
        this.r = r;
        this.resolver = resolver;
        this.newGame = newGame;
        helper = LoaderHelper.wrap(el);
        methodMap.clear();
        objectProps = Meterman2.gm.objectProps();

        loadScriptedMethods();

        loadBasicProperties();  // always load basic properties
        switch (defaultString(el.getAttributeValue("type"))) {  // and then perhaps class-specific properties
        case "dark":
            RoomImpl impl = r.getImpl();
            if (r instanceof DarkRoom)
                loadDarkRoomProperties((DarkRoom) r);
            break;
        }
        // So we don't accidentally see stale values.
        this.bundle = null;
        this.el = null;
        this.r = null;
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
                Meterman2.script.getScriptedMethods(r.getId(), bundle.getElementTextTrim(script));
            for (MMScript.ScriptedMethod sm : methods)
                methodMap.put(sm.getName(), sm);
        }
    }

    private void loadBasicProperties() {
        // Text properties
        r.setName(helper.getValue("name"));
        r.setExitName(helper.getValue("exitName"));
        final Element description = el.getChild("description");
        if (description != null)
            r.getImpl().setDescription(bundle.elementTextSource(description));

        // Attributes
        helper.loadAttributes("attributes", r.getAttributes());

        // Set a delegate if appropriate
        if (!methodMap.isEmpty()) {
            final ScriptedRoomImpl scriptedImpl = new ScriptedRoomImpl(r.getId(), methodMap);
            final EnumSet<RoomImpl.RoomMethod> roomMethodSet = scriptedImpl.getScriptedRoomMethods();
            if (!roomMethodSet.isEmpty())
                r.setDelegate(scriptedImpl, roomMethodSet);
        }

        if (newGame) {
            // Connections
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
            // Properties
            helper.setProps(r, objectProps);
        }
    }

    private void loadDarkRoomProperties(DarkRoom dr) {
        dr.setDarkName(helper.getValue("darkName"));
        final Element darkDescription = el.getChild("darkDescription");
        if (darkDescription != null)
            dr.setDarkDescription(bundle.elementTextSource(darkDescription));
        dr.setScriptedMethods(methodMap);
    }
}
