package com.illcode.meterman2.handler;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.Utils;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.event.LookListener;
import com.illcode.meterman2.loader.LoaderHelper;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.text.TextSource;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A handler that can add additional text to room descriptions when a player looks in
 * a given set of rooms.
 */
public class LookHandler implements LookListener
{
    private String handlerId;
    private Map<String, Pair<TextSource,Boolean>> roomTextMap;  // room ID -> text,paragraph

    /** Create a look-handler with the given ID. */
    public LookHandler(String handlerId) {
        this.handlerId = handlerId;
        roomTextMap = new HashMap<>(16);
    }

    /** Registers this look-handler with the game manager.  */
    public void register() {
        Meterman2.gm.addLookListener(this);
    }

    /** Deregisters this look-handler from the game manager.  */
    public void deregister() {
        Meterman2.gm.removeLookListener(this);
    }

    /**
     * Add an entry in our room map, so that the given text will be displayed when the
     * player looks in that room.
     * @param id room ID
     * @param text text-source to display
     * @param paragraph whether the text should be in its own paragraph
     */
    public void putEntry(String id, TextSource text, boolean paragraph) {
        roomTextMap.put(id, Pair.of(text, paragraph));
    }

    /** Remove a room entry from our map.
     *  @param id room ID */
    public void removeEntry(String id) {
        roomTextMap.remove(id);
    }

    /** Remove all room entries from our map. */
    public void clearEntries() {
        roomTextMap.clear();
    }

    /**
     * Loads room map entries from an XML element in a bundle.
     * @param b XBundle where element is found
     * @param id ID of the element to load from
     */
    public void loadFromElement(XBundle b, String id) {
        final Element el = b.getElement(id);
        if (el == null)
            return;
        LoaderHelper helper = LoaderHelper.wrap(el);
        for (Element group : el.getChildren("roomGroup")) {
            helper.setWrappedElement(group);
            final List<String> rooms = helper.getListValue("rooms");
            final Element text = group.getChild("text");
            if (text == null)
                continue;
            final boolean par = Utils.parseBoolean(text.getAttributeValue("paragraph"));
            for (String rid : rooms)
                putEntry(rid, b.elementTextSource(text), par);
        }
    }

    public void lookInRoom(Room currentRoom) {
        final Pair<TextSource,Boolean> pair = roomTextMap.get(currentRoom.getId());
        if (pair == null)
            return;
        final String text = pair.getLeft().getText();
        final boolean par = pair.getRight().booleanValue();
        Meterman2.gm.queueLookText(text, par);
    }

    public String getHandlerId() {
        return handlerId;
    }

    public Object getHandlerState() {
        return null;
    }

    public void restoreHandlerState(Object state) {
        // empty
    }

    public void gameStarting(boolean newGame) {
        // empty
    }
}
