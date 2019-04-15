package com.illcode.meterman2;

import com.illcode.meterman2.model.*;
import com.illcode.meterman2.text.TextSource;
import com.illcode.meterman2.ui.UIConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import com.illcode.meterman2.bundle.XBundle;

import java.util.*;

import static com.illcode.meterman2.SystemAttributes.CLOSED;
import static com.illcode.meterman2.SystemAttributes.LOCKED;

/**
 * Utility methods that apply to the game world or interface.
 */
public final class GameUtils
{
    private static final int DIALOG_DELAY_MS = Math.max(0, Utils.intPref("dialog-delay-ms", 200));

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
     * @param container the container
     * @return list of all contents
     */
    public static List<Entity> getEntitiesRecursive(EntityContainer container) {
        final List<Entity> entities = new ArrayList<>();
        gatherEntitiesRecursive(container, entities);
        return entities;
    }

    /**
     * Gather a list of all the entities in a given container, and if any of those entities is itself
     * a container, its contents recursively.
     * @param container the container
     * @param entities the collection into which we gather results
     */
    public static void gatherEntitiesRecursive(EntityContainer container, Collection<Entity> entities) {
        // I'm going to write this iteratively instead of recursively, just because.
        final Queue<EntityContainer> pendingContainers = new LinkedList<>();
        pendingContainers.add(container);
        while (!pendingContainers.isEmpty()) {
            EntityContainer c = pendingContainers.remove();
            for (Entity e : c.getEntities()) {
                entities.add(e);
                if (e instanceof EntityContainer)
                    pendingContainers.add((EntityContainer) e);
            }
        }
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
     * Load a dialog passage, which may specify its own header string, button label, image, and scale.
     * @param id ID of the dialog passage element in the system bundle group
     * @return a DialogPassage instance, or null if it could not be loaded
     */
    public static DialogPassage loadDialogPassage(String id) {
        final Pair<Element, XBundle> pair = Meterman2.bundles.getElementAndBundle(id);
        if (pair == null)
            return null;
        else
            return loadDialogPassage(pair.getRight(), pair.getLeft());
    }

    /**
     * Load a dialog passage, which may specify its own header string, button label, image, and scale.
     * @param b the bundle containing the element
     * @param e the element with the dialog passage definition
     * @return a DialogPassage instance, or null if it could not be loaded
     */
    public static DialogPassage loadDialogPassage(XBundle b, Element e) {
        if (b == null || e == null)
            return null;
        final String header = e.getAttributeValue("header", "");
        final String button = e.getAttributeValue("button", "Okay");
        final String image = e.getAttributeValue("image");
        final int scale = Utils.parseInt(e.getAttributeValue("scale"), 1);
        return new DialogPassage(header, button, image, scale, b.elementTextSource(e));
    }

    /**
     * Load a sequence of dialog passages, each of which may specify its own
     * header string, button label, image, and scale.
     * @param id ID of the dialog sequence element in the system bundle group.
     * @return a list of DialogPassage instances, or null if the sequence could not be loaded
     */
    public static DialogSequence loadDialogSequence(String id) {
        final Pair<Element, XBundle> pair = Meterman2.bundles.getElementAndBundle(id);
        if (pair == null)
            return null;
        else
            return loadDialogSequence(pair.getRight(), pair.getLeft());
    }

    /**
     * Load a sequence of dialog passages, each of which may specify its own
     * header string, button label, image, and scale.
     * @param b the bundle containing the element
     * @param e the element with the sequence definition
     * @return a list of DialogPassage instances, or null if the sequence could not be loaded
     */
    public static DialogSequence loadDialogSequence(XBundle b, Element e) {
        if (b == null || e == null)
            return null;
        final String defaultHeader = e.getAttributeValue("defaultHeader", "");
        final String defaultButton = e.getAttributeValue("defaultButton", "Okay");
        final String defaultImage = e.getAttributeValue("defaultImage");
        final int defaultScale = Utils.parseInt(e.getAttributeValue("defaultScale"), 1);
        final boolean noEscape = Utils.parseBoolean(e.getAttributeValue("noEscape"));

        final List<Element> entries = e.getChildren();
        final List<DialogPassage> dialogs = new ArrayList<>(entries.size());
        for (Element item : entries) {
            final String header = item.getAttributeValue("header", defaultHeader);
            final String button = item.getAttributeValue("button", defaultButton);
            final String image = item.getAttributeValue("image", defaultImage);
            final int scale = Utils.parseInt(item.getAttributeValue("scale"), defaultScale);
            final TextSource text = b.elementTextSource(item);
            dialogs.add(new DialogPassage(header, button, image, scale, text));
        }
        return new DialogSequence(dialogs, noEscape);
    }

    /** Show a sequence of dialog passages. */
    public static void showDialogSequence(DialogSequence sequence) {
        if (sequence == null)
            return;
        boolean first = true;
        for (DialogPassage dialog : sequence.dialogs) {
            if (DIALOG_DELAY_MS != 0) {
                // sleeping a little avoids a jarring flicker as each dialog transitions to the next
                if (!first) Utils.sleep(DIALOG_DELAY_MS);
                else first = false;
            }
            if (dialog.show() == -1 && !sequence.noEscape)
                break;  // allow the user to interrupt the sequence
        }
    }

    /**
     * Register actions and keyboard shortcuts found in an XML definition.
     * @param el XML definition element
     * @param system true if these are system actions
     */
    static void registerActions(Element el, boolean system) {
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
            if (shortcut != null && Meterman2.ui != null && system == Meterman2.actions.isSystemAction(a))
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
            final MMActions.Action a = Meterman2.actions.getAction(name);
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
        registerActions(el, false);
    }

    /**
     * Pushes a binding to both the script game namespace and template data model.
     * @param name variable name
     * @param value new value; if null, the binding will be removed.
     */
    public static void pushBinding(String name, Object value) {
        Meterman2.script.pushBinding(name, value);
        Meterman2.template.pushBinding(name, value);
    }

    /**
     * Restores the previously saved value of a variable in the script game namespace and template data model.
     * @param name variable name
     */
    public static void popBinding(String name) {
        Meterman2.script.popBinding(name);
        Meterman2.template.popBinding(name);
    }

    /**
     * Get the name of a room, taking into account darkness and dark-aware rooms.
     * @param r room
     * @return appropriate room name
     */
    public static String getRoomName(Room r) {
        String name;
        if (r.getAttributes().get(SystemAttributes.DARK) && r instanceof DarkAwareRoom)
            name = ((DarkAwareRoom) r).getDarkName();
        else
            name = r.getName();
        return name;
    }

    /**
     * Get the description of a room, taking into account darkness and dark-aware rooms.
     * @param r room
     * @return appropriate room description
     */
    public static String getRoomDescription(Room r) {
        String description;
        if (r.getAttributes().get(SystemAttributes.DARK)) {
            if (r instanceof DarkAwareRoom) {
                description = ((DarkAwareRoom) r).getDarkDescription();
            } else {
                GameUtils.pushBinding("room", r);
                description = Meterman2.bundles.getPassage("darkroom-description").getText();
                GameUtils.popBinding("room");
            }
        } else {
            description = r.getDescription();
        }
        return description;
    }

    /**
     * Get the entities in a room, taking into account darkness and dark-aware rooms.
     * @param r room
     * @return appropriate list of room entities
     */
    public static List<Entity> getRoomEntities(Room r) {
        List<Entity> entities;
        if (r.getAttributes().get(SystemAttributes.DARK)) {
            if (r instanceof DarkAwareRoom)
                entities = ((DarkAwareRoom) r).getDarkEntities();
            else
                entities = Collections.emptyList();
        } else {
            entities = r.getEntities();
        }
        return entities;
    }

    /**
     * Get passage text from the system bundle group.
     * @param id passage ID
     * @return text contained in the passage, or {@link XBundle#MISSING_TEXT_STRING} if no such passage is found.
     */
    public static String getPassage(String id) {
        return Meterman2.bundles.getPassage(id).getText();
    }

    /**
     * Get passage text from the system bundle group, with arguments.
     * @param id passage ID
     * @param args arguments for {@code TextSource.getTextWithArgs()}
     * @return text contained in the passage, or {@link XBundle#MISSING_TEXT_STRING} if no such passage is found.
     */
    public static String getPassageWithArgs(String id, Object... args) {
        return Meterman2.bundles.getPassage(id).getTextWithArgs(args);
    }

    /**
     * Get passage text from the system bundle group, with bindings.
     * @param id passage ID
     * @param bindings arguments for {@code TextSource.getTextWithBindings()}
     * @return text contained in the passage, or {@link XBundle#MISSING_TEXT_STRING} if no such passage is found.
     */
    public static String getPassageWithBindings(String id, String... bindings) {
        return Meterman2.bundles.getPassage(id).getTextWithBindings(bindings);
    }

    /** Print a passage. */
    public static void printPassage(String id) {
        Meterman2.gm.println(Meterman2.bundles.getPassage(id).getText());
    }

    /**
     * Print a passage, with arguments.
     * @param id passage ID
     * @param args arguments for {@code TextSource.getTextWithArgs()}
     */
    public static void printPassageWithArgs(String id, Object... args) {
        Meterman2.gm.println(Meterman2.bundles.getPassage(id).getTextWithArgs(args));
    }

    /**
     * Print a passage, with bindings.
     * @param id passage ID
     * @param bindings arguments for {@code TextSource.getTextWithBindings()}
     */
    public static void printPassageWithBindings(String id, String... bindings) {
        Meterman2.gm.println(Meterman2.bundles.getPassage(id).getTextWithBindings(bindings));
    }

    /**
     * Reads all bytes from a game asset and convert them to a string, assuming a UTF-8 encoding.
     * @param asset relative game asset path
     * @return string thus read, or null on error
     */
    public static String readGameAsset(String asset) {
        return Utils.readPath(Meterman2.assets.pathForGameAsset(asset));
    }

    /**
     * Return the collection of rooms that may be reached by exiting a given room.
     * @param r room
     * @param openDoors if true, return rooms accessible through closed but unlocked doors
     * @return collection of connected rooms
     */
    public static Collection<Room> getExitRooms(Room r, boolean openDoors) {
        final List<Room> rooms = new ArrayList<>(6);
        gatherExitRooms(r, openDoors, rooms);
        return rooms;
    }

    /**
     * Gather the list of rooms that may be reached by exiting a given room, taking
     * into account closed, unlocked doors.
     * @param r room
     * @param openDoors if true, gather rooms accessible through closed but unlocked doors
     * @param c collection into which we store our results
     */
    public static void gatherExitRooms(final Room r, boolean openDoors, final Collection<Room> c) {
        // First add all the normal exit neighbors of the room
        for (int direction = 0; direction < UIConstants.NUM_EXIT_BUTTONS; direction++) {
            final Room neighbor = r.getExit(direction);
            if (neighbor != null)
                c.add(neighbor);
        }
        if (openDoors) {
            for (Entity e : r.getEntities()) {
                EntityImpl impl = e.getImpl();
                if (impl instanceof DoorImpl) {
                    DoorImpl d = (DoorImpl) impl;
                    AttributeSet attr = e.getAttributes();
                    if (attr.get(CLOSED) && !attr.get(LOCKED)) {
                        final Room otherRoom = d.getOtherRoom(r);
                        if (otherRoom != null)
                            c.add(otherRoom);
                    }
                }
            }
        }
    }

    /**
     * A passage that can be displayed in a UI dialog.
     */
    public static final class DialogPassage
    {
        final String header;
        final String button;
        final String image;
        final int scale;
        final TextSource text;

        public DialogPassage(String header, String button, String image, int scale, TextSource text) {
            this.header = header;
            this.button = button;
            this.image = image;
            this.scale = scale;
            this.text = text;
        }

        /**
         * Shows the dialog passage.
         * @return 0 if the user closed the dialog by clicking the button, or -1 if the dialog
         *         was closed without selecting a button.
         */
        public int show() {
            if (image == null || image.equals("none"))
                return Meterman2.ui.showTextDialog(header, text.getText(), button);
            else
                return Meterman2.ui.showImageDialog(header, image, scale, text.getText(), button);
        }
    }

    public static final class DialogSequence
    {
        final List<DialogPassage> dialogs;
        final boolean noEscape;

        public DialogSequence(List<DialogPassage> dialogs, boolean noEscape) {
            this.dialogs = dialogs;
            this.noEscape = noEscape;
        }
    }
}
