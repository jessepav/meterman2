package com.illcode.meterman2.loader;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.model.Room;
import org.jdom2.Element;

public interface RoomLoader
{
    /**
     * Called by the game system to construct and return a Room (or subclass) with
     * properties defined in an XBundle element.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the room defintion
     * @param id ID for the new room
     * @return a new room
     */
    Room loadFromXml(XBundle bundle, Element el, String id);

    /**
     * Load values for the various room properties from an XML element.
     * Subclasses of RoomLoader implementations can chain up to their parent's method to load standard room
     * properties before loading subclass-specific properties. The implementation of hot-reloading
     * will likely end up using this method as well.
     * @param bundle XBundle that contains the element
     * @param el the XML element containing the room defintion
     * @param r the room whose properties will be set from values in the XML element.
     */
    void loadPropertiesFromXml(XBundle bundle, Element el, Room r);
}
