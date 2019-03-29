package com.illcode.meterman2;

import com.illcode.meterman2.model.Entity;
import com.illcode.meterman2.model.EntityContainer;
import com.illcode.meterman2.model.Room;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Utility methods that apply to the game world or interface.
 */
public final class GameUtils
{
    /** Convenience method to test if an entity has an attribute. */
    public static boolean hasAttr(Entity e, int attrNum) {
        return e.getAttributes().get(attrNum);
    }

    /** Convenience method to set an entity attribute. */
    public static void setAttr(Entity e, int attrNum) {
        e.getAttributes().set(attrNum);
    }

    /** Convenience method to set an entity attribute. */
    public static void setAttr(Entity e, int attrNum, boolean value) {
        e.getAttributes().set(attrNum, value);
    }

    /** Convenience method to test if a room has an attribute. */
    public static boolean hasAttr(Room r, int attrNum) {
        return r.getAttributes().get(attrNum);
    }

    /** Convenience method to set a room attribute. */
    public static void setAttr(Room r, int attrNum) {
        r.getAttributes().set(attrNum);
    }

    /** Convenience method to set a room attribute. */
    public static void setAttr(Room r, int attrNum, boolean value) {
        r.getAttributes().set(attrNum, value);
    }

    /**
     * Get the room in which an entity resides, following up the chain of containment if necessary.
     * @param e entity
     * @return the containing room, or null if the entity is not in a room.
     */
    public static Room getRoom(Entity e) {
        return getRoom(e.getContainer());
    }

    /**
     * Get the room in which container resides, following up the chain of containment if necessary.
     * @return the containing room, or null if the container is not in a room.
     */
    public static Room getRoom(EntityContainer container) {
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

    /**
     * Checks if a given container is a parent container of a given entity.
     * @param target the container to test for parenthood
     * @param e entity
     * @return true if <em>target</em> is a parent container of <em>e</em>
     */
    public static boolean isParentContainer(EntityContainer target, Entity e) {
        return isParentContainer(target, e.getContainer());
    }

    /**
     * Checks if a given container is a parent of another container.
     * @param target the container to test for parenthood
     * @param c container
     * @return true if <em>target</em> is a parent container of <em>c</em>
     */
    public static boolean isParentContainer(EntityContainer target, EntityContainer c) {
        while (c != null) {
            if (c == target)
                return true;
            else if (c == Meterman2.gm.getPlayer())  // only the room above the player remains
                return target == Meterman2.gm.getCurrentRoom();
            else if (c.getContainerType() == EntityContainer.CONTAINER_ENTITY)
                c = c.getContainerAsEntity().getContainer();
            else
                break;
        }
        return false;
    }

    /**
     * Return a list of all the entities in a given container, and if any of those entities is itself
     * a container, its contents recursively.
     */
    public static List<Entity> getEntitiesRecursive(EntityContainer container) {
        // I'm going to write this iteratively instead of recursively, just because.
        List<Entity> entities = new ArrayList<>();
        Queue<EntityContainer> pendingContainers = new LinkedList<>();
        pendingContainers.add(container);
        while (!pendingContainers.isEmpty()) {
            EntityContainer c = pendingContainers.remove();
            for (Entity e : c.getEntities()) {
                entities.add(e);
                if (e instanceof EntityContainer)
                    pendingContainers.add((EntityContainer) e);
            }
        }
        return entities;
    }

    /**
     * Puts an entity in a container removing it from its previous container, if it had one.
     * @param e entity
     * @param c container (may be null, to have the entity floating in the void).
     */
    public static void putInContainer(Entity e, EntityContainer c) {
        final EntityContainer oldC = e.getContainer();
        if (oldC != null)
            oldC.removeEntity(e);
        e.setContainer(c);
        if (c != null)
            c.addEntity(e);
    }

    /**
     * Returns the subset of a given list of entities that have a specific attribute value.
     * @param entities list of entities to filter
     * @param attrNum attribute to filter by
     * @param value the value of the attribute
     * @return a new list of entities that have {@code attribute = value}
     */
    public static List<Entity> filterByAttribute(List<Entity> entities, int attrNum, boolean value) {
        List<Entity> filteredList = new LinkedList<>();
        filterByAttribute(entities, attrNum, value, filteredList);
        return filteredList;
    }

    /**
     * Adds the subset of a given list of entities that have a specific attribute value to a target list.
     * @param entities list of entities to filter
     * @param attrNum attribute number to filter by
     * @param value the value of the attribute
     * @param target the list to which filtered entities will be added
     */
    public static void filterByAttribute(List<Entity> entities, int attrNum, boolean value, List<Entity> target) {
        for (Entity e : entities)
            if (e.getAttributes().get(attrNum) == value)
                target.add(e);
    }

    /**
     * Returns a list of all the {@link SystemAttributes#TAKEABLE} entities in the current room and the player
     * inventory. This is useful in situations, for instance, where some object has a slot that the
     * player can put something into. The object has an action "Put in Slot" that, when activated, will
     * prompt the player to choose what to put it -- and presumably only things that are <tt>TAKEABLE</tt> can
     * be lifted and put in.
     */
    public static List<Entity> getCurrentTakeableEntities() {
        List<Entity> takeables = new LinkedList<>();
        getCurrentTakeableEntities(takeables);
        return takeables;
    }

    /**
     * Like {@link #getCurrentTakeableEntities()}, but uses a list given as a parameter to avoid allocation.
     */
    public static void getCurrentTakeableEntities(List<Entity> takeables) {
        filterByAttribute(Meterman2.gm.getCurrentRoom().getEntities(), SystemAttributes.TAKEABLE, true, takeables);
        filterByAttribute(Meterman2.gm.getPlayer().getEntities(), SystemAttributes.TAKEABLE, true, takeables);
    }

    /**
     * Shows a passage, which may specify its own header string, button label, image, and scale.
     * <p/>
     * See <tt>xbundle-reference.xml</tt> for an example.
     * @param id ID of the passage in the system bundle group
     */
    public static void showPassage(String id) {
        final Element e = Meterman2.bundles.getElement(id);
        if (e == null || !e.getName().equals("passage"))
            return;
        final String header = e.getAttributeValue("header", "");
        final String button = e.getAttributeValue("button", "Okay");
        final String image = e.getAttributeValue("image");
        final int scale = Utils.parseInt(e.getAttributeValue("scale"), 1);
        final String text = Meterman2.bundles.getPassage(id).getText();
        if (image == null)
            Meterman2.ui.showTextDialog(header, text, button);
        else
            Meterman2.ui.showImageDialog(header, image, scale, text, button);
    }

    /**
     * Shows a sequence of passages, each of which may specify its own
     * header string, button label, image, and scale.
     * <p/>
     * See <tt>xbundle-reference.xml</tt> for an example.
     * @param id ID of the passage sequence element in the system bundle group.
     */
    public static void showPassageSequence(String id) {
        final Element e = Meterman2.bundles.getElement(id);
        if (e == null)
            return;
        String defaultHeader = e.getAttributeValue("defaultHeader", "");
        String defaultButton = e.getAttributeValue("defaultButton", "Okay");
        for (Element item : e.getChildren()) {
            final String passageId = item.getAttributeValue("passageId");
            if (passageId == null)
                continue;
            final String header = item.getAttributeValue("header", defaultHeader);
            final String button = item.getAttributeValue("button", defaultButton);
            final String image = e.getAttributeValue("image");
            final int scale = Utils.parseInt(e.getAttributeValue("scale"), 1);
            final String text = Meterman2.bundles.getPassage(passageId).getText();
            if (image == null)
                Meterman2.ui.showTextDialog(header, text, button);
            else
                Meterman2.ui.showImageDialog(header, image, scale, text, button);
        }
    }

    /**
     * Register actions and keyboard shortcuts found in an XML definition.
     * @param el XML definition element
     * @param system true if these are system actions
     * @param putShortcuts true if the shortcuts should be installed in the UI
     */
    static void registerActions(Element el, boolean system, boolean putShortcuts) {
        for (Element actionEl : el.getChildren("action")) {
            final String name = actionEl.getAttributeValue("name");
            final String templateText = actionEl.getAttributeValue("templateText");
            final String shortcut = actionEl.getAttributeValue("shortcut");
            if (name == null || templateText == null)
                continue;
            MMActions.Action a = Meterman2.actions.getAction(name);
            if (a == null) {  // register a new action
                if (system)
                    a = Meterman2.actions.registerSystemAction(name, templateText);
                else
                    a = Meterman2.actions.registerAction(name, templateText);
            } else {  // just change the templateText
                a.setTemplateText(templateText);
                a.setFixedText(null);
            }
            // Don't let games override bindings for system actions
            if (putShortcuts && shortcut != null && system == Meterman2.actions.isSystemAction(a))
                Meterman2.ui.putActionBinding(a, shortcut);
        }
    }

    /** Set shortcuts for already-registered actions.
     *  @param el XML definition element */
    static void setActionShortcuts(Element el) {
        for (Element actionEl : el.getChildren("action")) {
            final String name = actionEl.getAttributeValue("name");
            final String shortcut = actionEl.getAttributeValue("shortcut");
            if (name == null || shortcut == null)
                continue;
            MMActions.Action a = Meterman2.actions.getAction(name);
            if (a == null)
                continue;
            Meterman2.ui.putActionBinding(a, shortcut);
        }
    }

    /**
     * Register game actions and keyboard shortcuts found in an XML definition.
     * @param el XML definition element
     */
    public static void registerActions(Element el) {
        registerActions(el, false, true);
    }
}