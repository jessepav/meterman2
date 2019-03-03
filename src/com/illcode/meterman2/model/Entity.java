package com.illcode.meterman2.model;

/**
 * The "interface" through which the game system and UI interacts with entities.
 * <p/>
 * Entity is a class rather than an interface because it supports method delegation and attributes; all
 * other implementation is handled by an instance of {@link EntityImpl}. It is that interface, and its base
 * implementation {@code BaseEntityImpl}, that specialized entities will usually extend.
 */
public class Entity
{
}
