package com.illcode.meterman2.model;

import com.illcode.meterman2.MMAttributes.AttributeSet;

/**
 * The "interface" through which the game system and UI interacts with entities.
 * <p/>
 * Entity is a class rather than an interface because it supports method delegation and attributes; all
 * other implementation is handled by an instance of {@link EntityImpl}. It is that interface, and its base
 * implementation {@code BaseEntityImpl}, that specialized entities will usually extend.
 */
public class Entity
{
    public AttributeSet getAttributes() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String getIndefiniteArticle() {
        return null;
    }
}
