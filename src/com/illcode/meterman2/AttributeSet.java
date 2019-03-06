package com.illcode.meterman2;

import java.util.BitSet;

/**
 * A set of attributes to be associated with a game object.
 */
public class AttributeSet
{
    private BitSet bits;

    /** Create an empty AttributeSet. */
    public AttributeSet() {
        bits = new BitSet();
    }

    /** Return true if this AttributeSet has a given attribute set. */
    public boolean get(int attrNum) {
        return bits.get(attrNum);
    }

    /** Set an attribute to true. */
    public void set(int attrNum) {
        bits.set(attrNum);
    }

    /** Set an attribute to the given value. */
    public void set(int attrNum, boolean value) {
        bits.set(attrNum, value);
    }

    /** Set multiple attributes to true.*/
    public void setMultiple(int... attributes) {
        for (int j : attributes)
            bits.set(j);
    }

    /** Clear all attributes. */
    public void clear() {
        bits.clear();
    }

    /** Return a new AttributeSet that is a copy of the given set. */
    public static AttributeSet copyOf(AttributeSet set) {
        AttributeSet newSet = new AttributeSet();
        newSet.bits = (BitSet) set.bits.clone();
        return newSet;
    }
}
