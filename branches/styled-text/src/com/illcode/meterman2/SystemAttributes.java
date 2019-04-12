package com.illcode.meterman2;

/**
 * Attributes used by the game system (as opposed to a specific game's attributes).
 * <p/>
 * The string representation of an attribute is the field name converted to lowercase
 * with underscores replaced by dashes (ex. <tt>PROPER_NAME</tt> -&gt <tt>"proper-name"</tt>).
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

    /** Something that can be pulled or pushed. */
    public static int MOVEABLE;

    /** The entity (ex. door, container) is closed. */
    public static int CLOSED;

    /** The entity (ex. door, container) is locked. */
    public static int LOCKED;

    /** The switchable entity is on (as opposed to off). */
    public static int ON;

    //
    // Room attributes
    //

    /** Indicates if a room has been visited before */
    public static int VISITED;

    /** This room is naturally dark. */
    public static int DARK;

    //
    // Applicable to both
    //

    /** This entity or room has a proper name and doesn't require the definite article. */
    public static int PROPER_NAME;

    public static void init() {
        CONCEALED = Meterman2.attributes.registerAttribute("concealed");
        TAKEABLE = Meterman2.attributes.registerAttribute("takeable");
        EQUIPPABLE = Meterman2.attributes.registerAttribute("equippable");
        LIGHTSOURCE = Meterman2.attributes.registerAttribute("lightsource");
        MOVEABLE = Meterman2.attributes.registerAttribute("moveable");
        CLOSED = Meterman2.attributes.registerAttribute("closed");
        LOCKED = Meterman2.attributes.registerAttribute("locked");
        ON = Meterman2.attributes.registerAttribute("on");

        VISITED = Meterman2.attributes.registerAttribute("visited");
        DARK = Meterman2.attributes.registerAttribute("dark");

        PROPER_NAME = Meterman2.attributes.registerAttribute("proper-name");

        Meterman2.attributes.markSystemAttributesDone();
    }
}
