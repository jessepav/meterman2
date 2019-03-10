package com.illcode.meterman2;

import java.util.BitSet;

/**
 * A set of attributes to be associated with a game object.
 */
public class AttributeSet
{
    private BitSet bits;

    private AttributeSet() {
    }

    /** Create an empty AttributeSet. */
    public static AttributeSet create() {
        AttributeSet as = new AttributeSet();
        as.bits = new BitSet();
        return as;
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

    /** Set multiple attributes to true. */
    public void setMultiple(int... attributes) {
        for (int j : attributes)
            bits.set(j);
    }

    /** Clear all attributes. */
    public void clear() {
        bits.clear();
    }

    /** Return a new AttributeSet that is a copy of this set. */
    public AttributeSet copy() {
        AttributeSet newSet = new AttributeSet();
        newSet.bits = (BitSet) bits.clone();
        return newSet;
    }

    /**
     * Get a view of this attribute set as a BitSet. Changes to this attribute set will be
     * reflected in the bit-set, and vice versa.
     * @return BitSet view of this attribute set.
     */
    public BitSet asBitSet() {
        return bits;
    }

    /** Return this attribute set as a byte array. */
    public byte[] toByteArray() {
        return bits.toByteArray();
    }

    // The shiftLeft() and shiftRight() methods were last present in SVN revision 91,
    // if for some reason you need them again.

    /** Return a new attribute set from a byte array. */
    public static AttributeSet fromByteArray(byte[] bytes) {
        AttributeSet as = new AttributeSet();
        as.bits = BitSet.valueOf(bytes);
        return as;
    }
}
