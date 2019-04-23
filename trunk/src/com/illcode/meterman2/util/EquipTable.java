package com.illcode.meterman2.util;

import com.illcode.meterman2.loader.LoaderHelper;
import com.illcode.meterman2.model.Entity;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for managing the category of equippables and the limits for each.
 */
public final class EquipTable
{
    public static final int DEFAULT_LIMIT = 4;

    private Map<Entity,Category> categoryMap;

    /** Construct an empty table. */
    public EquipTable() {
        categoryMap = new HashMap<>();
    }

    /** Put an entity into the table under a given category. */
    public void putEntity(Entity e, Category c) {
        categoryMap.put(e, c);
    }

    /** Remove an entity from the table. */
    public void removeEntity(Entity e) {
        categoryMap.remove(e);
    }

    /** Get the category of an entity, or null if it isn't in the table. */
    public Category getCategory(Entity e) {
        return categoryMap.get(e);
    }

    /** Clear the table. */
    public void clear() {
        categoryMap.clear();
    }

    /**
     * Load an equip-table from a bundle element.
     * @param el element
     * @param entityIdMap map from entity ID to entity
     */
    public void loadFromElement(Element el, Map<String,Entity> entityIdMap) {
        if (el == null)
            return;
        final LoaderHelper helper = LoaderHelper.wrap(el);
        for (Element category : el.getChildren("category")) {
            helper.setWrappedElement(category);
            final String name = helper.getValue("name");
            final int limit = helper.getIntValue("limit", DEFAULT_LIMIT);
            final String limitMessage = helper.getValue("limitMessage");
            final List<String> ids = helper.getListValue("items");
            if (name == null || limitMessage == null || ids.isEmpty())
                continue;
            final List<Entity> entities = new ArrayList<>(ids.size());
            for (String id : ids) {
                final Entity e = entityIdMap.get(id);
                if (e != null)
                    entities.add(e);
            }
            if (entities.isEmpty())
                continue;
            Category c = new Category(name, limit, limitMessage);
            for (Entity e : entities)
                putEntity(e, c);
        }
    }

    /** Create an equip-table and load its values from a bundle element. */
    public static EquipTable createFromElement(Element el, Map<String,Entity> entityIdMap) {
        final EquipTable table = new EquipTable();
        table.loadFromElement(el, entityIdMap);
        return table;
    }

    /**
     * A category for equippable entities. Note that categories are compared by identity in the
     * game system, so each category should be represented by a single instance. This is done
     * properly if the {@link #loadFromElement} method is used.
     */
    public static final class Category
    {
        /** The name of the category. */
        public final String name;

        /** The limit of how many entities in this category can be equipped at one time. */
        public final int limit;

        /** The message that should be displayed if the player attempts to equip more entities
            than is allowed for this category. */
        public final String limitMessage;

        public Category(String name, int limit, String limitMessage) {
            this.name = name;
            this.limit = limit;
            this.limitMessage = limitMessage;
        }
    }
}
