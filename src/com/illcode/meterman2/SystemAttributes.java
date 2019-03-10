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

    /** The entity is not quite small enough to be carried, but can be pushed or pulled along the floor. */
    public static int MOVEABLE;

    //
    // Room attributes
    //

    /** Indicates if a room has been visited before */
    public static int VISITED;

    /** This room is naturally dark. */
    public static int DARK;

    public static void init() {
        // We register the attributes that are more likely to be set before those less likely,
        // because when we persist attributes, it is the highest set attribute number that determines
        // the size of the bit-set.
        VISITED = Meterman2.attributes.registerAttribute("visited");
        TAKEABLE = Meterman2.attributes.registerAttribute("takeable");
        EQUIPPABLE = Meterman2.attributes.registerAttribute("equippable");
        DARK = Meterman2.attributes.registerAttribute("dark");
        PROPER_NAME = Meterman2.attributes.registerAttribute("proper-name");
        CONCEALED = Meterman2.attributes.registerAttribute("concealed");
        LIGHTSOURCE = Meterman2.attributes.registerAttribute("lightsource");
        MOVEABLE = Meterman2.attributes.registerAttribute("moveable");

        Meterman2.attributes.markSystemAttributesDone();
    }
}
