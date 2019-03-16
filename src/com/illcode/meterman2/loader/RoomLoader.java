package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.GameObjectIdResolver;
import com.illcode.meterman2.model.Room;
import org.jdom2.Element;

/**
 * An object that can instantiate and load rooms based on an XML definition.
 */
public interface RoomLoader
{
    /**
     * Called by the game system to construct and return a Room (or subclass) with
     * based on an XML definition. It does not load properties into the room --
     * that is performed by {@link #loadRoomProperties}.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the room defintion
     * @param id ID for the new room
     * @return a new room
     */
    Room createRoom(XBundle bundle, Element el, String id);

    /**
     * Load values for the various room properties from an XML element.
     * Subclasses of RoomLoader implementations can chain up to their parent's method to load standard room
     * properties before loading subclass-specific properties. The implementation of hot-reloading
     * will likely end up using this method as well.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the room defintion
     * @param r the room whose properties will be set from values in the XML element.
     * @param resolver used to resolve references in the room, for instance if it refers to
*                 a door, or its connections to other rooms.
     */
    void loadRoomProperties(XBundle bundle, Element el, Room r, GameObjectIdResolver resolver);
}
