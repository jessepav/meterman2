package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemAttributes;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Utility methods that apply to the game world or interface.
 */
public final class GameUtils
{
    /**
     * Return the name of the entity prefixed by the definite article ("the"), taking into
     * account proper names.
     * @param e entity
     * @param capitalize whether to capitalize the definite article
     */
    public static String defName(Entity e, boolean capitalize) {
        String name = e.getName();
        if (name.isEmpty())
            return "";
        if (e.getAttributes().get(SystemAttributes.PROPER_NAME))
            return name;
        String defArt = capitalize ? "The " : "the ";
        return defArt + name;
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the") in lowercase,
     * taking into account proper names.
     * @param e entity
     */
    public static String defName(Entity e) {
        return defName(e, false);
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other"), taking into account
     * proper names. If {@code e.getIndefiniteArticle()} returns null, we use "an" for names that begin with
     * a vowel, and "a" otherwise.
     * @param e entity
     * @param capitalize whether to capitalize the indefinite article
     */
    public static String indefName(Entity e, boolean capitalize) {
        String name = e.getName();
        if (name.isEmpty())
            return "";
        if (e.getAttributes().get(SystemAttributes.PROPER_NAME))
            return name;
        String indefArt = e.getIndefiniteArticle();
        if (indefArt == null) {
            char c = Character.toLowerCase(name.charAt(0));
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u')
                indefArt = "an";
            else
                indefArt = "a";
        }
        if (capitalize)
            indefArt = WordUtils.capitalize(indefArt);
        return indefArt + " " + name;
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other") in lowercase.
     * @param e entity
     */
    public static String indefName(Entity e) {
        return indefName(e, false);
    }

    /** Convenience method to test if an entity has an attribute. */
    public static boolean hasAttr(Entity e, int attrNum) {
        return e.getAttributes().get(attrNum);
    }

    /** Convenience method to test if a room has an attribute. */
    public static boolean hasAttr(Room r, int attrNum) {
        return r.getAttributes().get(attrNum);
    }

    /** Convenience method to set an entity attribute. */
    public static void setAttr(Entity e, int attrNum) {
        e.getAttributes().set(attrNum);
    }

    /** Convenience method to set a room attribute. */
    public static void setAttr(Room r, int attrNum) {
        r.getAttributes().set(attrNum);
    }

    /**
     * Get the room in which an entity resides, following up the chain of containment if necessary.
     * @param e entity
     * @return the containing room, or null if the entity is not in a room.
     */
    public static Room getEntityRoom(Entity e) {
        EntityContainer container = e.getContainer();
        while (container != null) {
            switch (container.getContainerType()) {
            case EntityContainer.CONTAINER_ROOM:
                return container.getContainerAsRoom();
            case EntityContainer.CONTAINER_PLAYER:
                return Meterman2.gm.getCurrentRoom();
            case EntityContainer.CONTAINER_ENTITY:  // climb up the chain
                container = container.getContainerAsEntity().getContainer();
                break;
            default: // something is wrong
                return null;
            }
        }
        return null;
    }
}
