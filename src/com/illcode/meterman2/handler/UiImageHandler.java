package com.illcode.meterman2.handler;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.event.EntitySelectionListener;
import com.illcode.meterman2.event.PlayerMovementListener;
import com.illcode.meterman2.loader.LoaderHelper;
import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.Room;
import com.illcode.meterman2.ui.UIConstants;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A handler that displays a frame image based on the current room, or an entity image
 * depending on the selected entity. Use one of the two static builder methods to get
 * the appropriate version of the handler.
 */
public class UiImageHandler implements PlayerMovementListener, EntitySelectionListener
{
    private String handlerId;
    private Map<String,String> imageMap;
    private String defaultImage;

    private String idAttributeName;
    private boolean isFrameImageHandler;

    private UiImageHandler(String handlerId) {
        this.handlerId = handlerId;
        imageMap = new HashMap<>();
        defaultImage = UIConstants.NO_IMAGE;
    }

    /** Create an image handler that will handle frame images. */
    public static UiImageHandler createFrameImageHandler(String handlerId) {
        UiImageHandler handler = new UiImageHandler(handlerId);
        handler.idAttributeName = "rooms";
        handler.isFrameImageHandler = true;
        return handler;
    }

    /** Create an image handler that will handle entity images. */
    public static UiImageHandler createEntityImageHandler(String handlerId) {
        UiImageHandler handler = new UiImageHandler(handlerId);
        handler.idAttributeName = "entities";
        handler.isFrameImageHandler = false;
        return handler;
    }

    /** Registers this handler with the game manager.  */
    public void register() {
        if (isFrameImageHandler)
            Meterman2.gm.addPlayerMovementListener(this);
        else
            Meterman2.gm.addEntitySelectionListener(this);
    }

    /** Deregisters this handler from the game manager.  */
    public void deregister() {
        if (isFrameImageHandler)
            Meterman2.gm.removePlayerMovementListener(this);
        else
            Meterman2.gm.removeEntitySelectionListener(this);
    }

    /**
     * Add an entry in our image map, so that either:
     * <ul>
     *     <li>the given frame image will be displayed when the player is in the given room</li>
     *     <li>the given entity image will be displayed when the user selects the given entity</li>
     * </ul>
     * @param id room or entity ID
     * @param name name of the frame or entity image. It can be <tt>"none"</tt>, for no image;
     * or, for frame images only, <tt>"default"</tt> to specify the default frame image.
     */
    public void putEntry(String id, String name) {
        imageMap.put(id, validateImageName(name));
    }

    /** Remove an entry from our image map.
     *  @param id room or entity ID */
    public void removeEntry(String id) {
        imageMap.remove(id);
    }

    /** Remove all entries from our image map. */
    public void clearEntries() {
        imageMap.clear();
    }

    /**
     * Set the image shown if we don't have an entry for the current room or selected entity in our map.
     * @param name name of the image.
     */
    public void setDefaultImage(String name) {
        defaultImage = validateImageName(name);
    }

    private String validateImageName(String name) {
        if (name == null || name.isEmpty())
            name = UIConstants.NO_IMAGE;
        else if (name.equals("none"))
            name = UIConstants.NO_IMAGE;
        else if (isFrameImageHandler && name.equals("default"))
            name = UIConstants.DEFAULT_FRAME_IMAGE;
        return name;
    }

    /**
     * Loads image map entries from an XML element in a bundle.
     * @param b XBundle where element is found
     * @param id ID of the element to load from
     */
    public void loadFromElement(XBundle b, String id) {
        final Element el = b.getElement(id);
        if (el == null)
            return;
        final LoaderHelper helper = LoaderHelper.wrap(el);
        setDefaultImage(helper.getValue("defaultImage"));
        for (Element entry : el.getChildren("image")) {
            helper.setWrappedElement(entry);
            final String imageName = helper.getValue("name");
            final List<String> objectIds = helper.getListValue(idAttributeName);
            if (imageName == null || objectIds.isEmpty())
                continue;
            for (String oid : objectIds)
                putEntry(oid, imageName);
        }
    }

    // Only called if we're handling frame images.
    public boolean playerMove(Room fromRoom, Room toRoom, boolean beforeMove) {
        if (!beforeMove)  // we're only interested in actual player movement
            setFrameImage(toRoom.getId());
        return false;
    }

    private void setFrameImage(String roomId) {
        String imageName = imageMap.get(roomId);
        if (imageName == null)
            imageName = defaultImage;
        Meterman2.ui.setFrameImage(imageName);
    }

    // Only called if we're handling entity images.
    public boolean entitySelected(Entity e) {
        String imageName = imageMap.get(e.getId());
        if (imageName == null)
            imageName = defaultImage;
        Meterman2.ui.setEntityImage(imageName);
        return false;
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
        if (isFrameImageHandler)
            setFrameImage(Meterman2.gm.getCurrentRoom().getId());
        // Games always start with no entity selected, so we don't need to do anything.
    }
}
