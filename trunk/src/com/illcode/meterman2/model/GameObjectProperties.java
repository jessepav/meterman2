package com.illcode.meterman2.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages custom properties can be attached to an entity or room.
 */
public final class GameObjectProperties
{
    private Map<Entity,Map<String,Object>> entityPropertyMap;
    private Map<Room,Map<String,Object>> roomPropertyMap;
    private Map<String,Object> defaultPropertyMap;

    /**
     * Create an empty game object properties instance.
     */
    public GameObjectProperties() {
        entityPropertyMap = new HashMap<>();
        roomPropertyMap = new HashMap<>();
        defaultPropertyMap = new HashMap<>();
    }

    /** Intended for internal serialization purposes. */
    public Map<Entity,Map<String,Object>> getEntityPropertyMap() {
        return entityPropertyMap;
    }

    /** Intended for internal serialization purposes. */
    public Map<Room,Map<String,Object>> getRoomPropertyMap() {
        return roomPropertyMap;
    }

    /** Intended for internal serialization purposes. */
    public void restoreFromIdMaps(HashMap<String,HashMap<String,Object>> entityIdPropertyMap,
                                  HashMap<String,HashMap<String,Object>> roomIdPropertyMap,
                                  Map<String,Entity> entityIdMap,
                                  Map<String,Room> roomIdMap) {
        for (Map.Entry<String,HashMap<String,Object>> entry : entityIdPropertyMap.entrySet()) {
            final Map<String,Object> m = entry.getValue();
            final Entity e = entityIdMap.get(entry.getKey());
            if (e != null && m != null)
                entityPropertyMap.put(e, m);
        }
        for (Map.Entry<String,HashMap<String,Object>> entry : roomIdPropertyMap.entrySet()) {
            final Map<String,Object> m = entry.getValue();
            final Room r = roomIdMap.get(entry.getKey());
            if (r != null && m != null)
                roomPropertyMap.put(r, m);
        }
    }

    /** Intended for internal serialization purposes. */
    public void saveToIdMaps(HashMap<String,HashMap<String,Object>> entityIdPropertyMap,
                             HashMap<String,HashMap<String,Object>> roomIdPropertyMap) {
        for (Map.Entry<Entity,Map<String,Object>> entry : entityPropertyMap.entrySet()) {
            final Map<String,Object> m = entry.getValue();
            if (m != null)
                entityIdPropertyMap.put(entry.getKey().getId(), new HashMap<>(m));
        }
        for (Map.Entry<Room,Map<String,Object>> entry : roomPropertyMap.entrySet()) {
            final Map<String,Object> m = entry.getValue();
            if (m != null)
                roomIdPropertyMap.put(entry.getKey().getId(), new HashMap<>(m));
        }
    }

    /**
     * Clear all properties.
     */
    public void clear() {
        entityPropertyMap.clear();
        roomPropertyMap.clear();
        defaultPropertyMap.clear();
    }

    /** Set a default string property. */
    public void setDefaultProp(String name, String value) {
        defaultPropertyMap.put(name, value);
    }

    /** Set a default integer property. */
    public void setDefaultIntProp(String name, int value) {
        defaultPropertyMap.put(name, Integer.valueOf(value));
    }

    /** Set a default boolean property. */
    public void setDefaultBooleanProp(String name, boolean value) {
        defaultPropertyMap.put(name, Boolean.valueOf(value));
    }

    /**
     * Set an entity property
     * @param e entity
     * @param name property name
     * @param value property value
     */
    private void setEntityProp(Entity e, String name, Object value) {
        Map<String,Object> m = entityPropertyMap.get(e);
        if (m == null) {
            m = new HashMap<>(8);
            entityPropertyMap.put(e, m);
        }
        m.put(name, value);
    }

    /**
     * Set a room property
     * @param e entity
     * @param name property name
     * @param value property value
     */
    private void setRoomProp(Room r, String name, Object value) {
        Map<String,Object> m = roomPropertyMap.get(r);
        if (m == null) {
            m = new HashMap<>(8);
            roomPropertyMap.put(r, m);
        }
        m.put(name, value);
    }

    /**
     * Set a string entity property.
     * @param e entity
     * @param name property name
     * @param value property value
     */
    public void setProp(Entity e, String name, String value) {
        setEntityProp(e, name, value);
    }

    /**
     * Set an integer entity property.
     * @param e entity
     * @param name property name
     * @param value property value
     */
    public void setIntProp(Entity e, String name, int value) {
        setEntityProp(e, name, Integer.valueOf(value));
    }

    /**
     * Set a boolean entity property.
     * @param e entity
     * @param name property name
     * @param value property value
     */
    public void setBooleanProp(Entity e, String name, boolean value) {
        setEntityProp(e, name, Boolean.valueOf(value));
    }

    /**
     * Set a string room property.
     * @param r room
     * @param name property name
     * @param value property value
     */
    public void setProp(Room r, String name, String value) {
        setRoomProp(r, name, value);
    }

    /**
     * Set an integer room property.
     * @param r room
     * @param name property name
     * @param value property value
     */
    public void setIntProp(Room r, String name, int value) {
        setRoomProp(r, name, Integer.valueOf(value));
    }

    /**
     * Set a boolean room property.
     * @param r room
     * @param name property name
     * @param value property value
     */
    public void setBooleanProp(Room r, String name, boolean value) {
        setRoomProp(r, name, Boolean.valueOf(value));
    }

    /**
     * Get an entity property.
     * @param e entity
     * @param name property name
     * @return property value or default value if not found; if there is no default
     * value either, return null.
     */
    private Object getEntityProp(Entity e, String name) {
        Object val = null;
        final Map<String,Object> m = entityPropertyMap.get(e);
        if (m != null)
            val = m.get(name);
        if (val == null)
            val = defaultPropertyMap.get(name);
        return val;
    }

    /**
     * Get a room property.
     * @param r room
     * @param name property name
     * @return property value or default value if not found; if there is no default
     * value either, return null.
     */
    private Object getRoomProp(Room r, String name) {
        Object val = null;
        final Map<String,Object> m = roomPropertyMap.get(r);
        if (m != null)
            val = m.get(name);
        if (val == null)
            val = defaultPropertyMap.get(name);
        return val;
    }

    /**
     * Get a string entity property.
     * @param e entity
     * @param name property name
     * @param defaultVal default value
     * @return string property value or default value if not found.
     */
    public String getProp(Entity e, String name, String defaultVal) {
        final Object o = getEntityProp(e, name);
        if (o == null)
            return defaultVal;
        else if (o instanceof String)
            return (String) o;
        else
            return o.toString();
    }

    /**
     * Get a string entity property.
     * @param e entity
     * @param name property name
     * @return string property value or null if not found.
     */
    public String getProp(Entity e, String name) {
        return getProp(e, name, null);
    }

    /**
     * Get an integer entity property.
     * @param e entity
     * @param name property name
     * @param defaultVal default value
     * @return integer property value or default value if not found.
     */
    public int getIntProp(Entity e, String name, int defaultVal) {
        final Object o = getEntityProp(e, name);
        if (o instanceof Integer)
            return ((Integer) o).intValue();
        else
            return defaultVal;
    }

    /**
     * Get an integer entity property.
     * @param e entity
     * @param name property name
     * @return integer property value or -1 if not found.
     */
    public int getIntProp(Entity e, String name) {
        return getIntProp(e, name, -1);
    }

    /**
     * Get a boolean entity property.
     * @param e entity
     * @param name property name
     * @param defaultVal default value
     * @return boolean property value or default value if not found.
     */
    public boolean getBooleanProp(Entity e, String name, boolean defaultVal) {
        final Object o = getEntityProp(e, name);
        if (o instanceof Boolean)
            return ((Boolean) o).booleanValue();
        else
            return defaultVal;
    }

    /**
     * Get a boolean entity property.
     * @param e entity
     * @param name property name
     * @return boolean property value or false if not found.
     */
    public boolean getBooleanProp(Entity e, String name) {
        return getBooleanProp(e, name, false);
    }

    /**
     * Get a string room property.
     * @param r room
     * @param name property name
     * @param defaultVal default value
     * @return string property value or default value if not found.
     */
    public String getProp(Room r, String name, String defaultVal) {
        final Object o = getRoomProp(r, name);
        if (o == null)
            return defaultVal;
        else if (o instanceof String)
            return (String) o;
        else
            return o.toString();
    }

    /**
     * Get a string room property.
     * @param r room
     * @param name property name
     * @return string property value or null if not found.
     */
    public String getProp(Room r, String name) {
        return getProp(r, name, null);
    }

    /**
     * Get an integer room property.
     * @param r room
     * @param name property name
     * @param defaultVal default value
     * @return integer property value or default value if not found.
     */
    public int getIntProp(Room r, String name, int defaultVal) {
        final Object o = getRoomProp(r, name);
        if (o instanceof Integer)
            return ((Integer) o).intValue();
        else
            return defaultVal;
    }

    /**
     * Get an integer room property.
     * @param r room
     * @param name property name
     * @return integer property value or -1 if not found.
     */
    public int getIntProp(Room r, String name) {
        return getIntProp(r, name, -1);
    }

    /**
     * Get a boolean room property.
     * @param r room
     * @param name property name
     * @param defaultVal default value
     * @return boolean property value or default value if not found.
     */
    public boolean getBooleanProp(Room r, String name, boolean defaultVal) {
        final Object o = getRoomProp(r, name);
        if (o instanceof Boolean)
            return ((Boolean) o).booleanValue();
        else
            return defaultVal;
    }

    /**
     * Get a boolean room property.
     * @param r room
     * @param name property name
     * @return boolean property value or false if not found.
     */
    public boolean getBooleanProp(Room r, String name) {
        return getBooleanProp(r, name, false);
    }
}
