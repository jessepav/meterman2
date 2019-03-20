package com.illcode.meterman2;

/**
 * Attributes used by the game system (as opposed to a specific game's attributes).
 */
public class SystemAttributes
{
    //
    // Entity attributes
    //

    /** If set, the entity will not be displayed in the room entities list. */
    public static int CONCEALED;

    /** If set, the entity can be taken (i.e. transferred to the player inventory) and dropped */
    public static int TAKEABLE;

    /** The entity is equippable */
    public static int EQUIPPABLE;

    /** This entity is a light source. */
    public static int LIGHTSOURCE;

    /** This entity has a proper name and doesn't require the definite article.
     *  The string representation for this attribute is {@code "proper-name"}. */
    public static int PROPER_NAME;

    /** Something that can be pulled or pushed. */
    public static int MOVEABLE;

    /** The entity (ex. door, container) is closed. */
    public static int CLOSED;

    /** The entity (ex. door, container) is locked. */
    public static int LOCKED;

    //
    // Room attributes
    //

    /** Indicates if a room has been visited before */
    public static int VISITED;

    /** This room is naturally dark. */
    public static int DARK;

    public static void init() {
        CONCEALED = Meterman2.attributes.registerAttribute("concealed");
        TAKEABLE = Meterman2.attributes.registerAttribute("takeable");
        EQUIPPABLE = Meterman2.attributes.registerAttribute("equippable");
        LIGHTSOURCE = Meterman2.attributes.registerAttribute("lightsource");
        PROPER_NAME = Meterman2.attributes.registerAttribute("proper-name");
        MOVEABLE = Meterman2.attributes.registerAttribute("moveable");
        CLOSED = Meterman2.attributes.registerAttribute("closed");
        LOCKED = Meterman2.attributes.registerAttribute("locked");

        VISITED = Meterman2.attributes.registerAttribute("visited");
        DARK = Meterman2.attributes.registerAttribute("dark");

        Meterman2.attributes.markSystemAttributesDone();
    }
}
