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

    /** Set multiple attributes to true.*/
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

    /** Return this attribute set as a byte array. */
    public byte[] toByteArray() {
        return bits.toByteArray();
    }

    /**
     * Shift the bits representing this attribute set to the left; that is, bits will be moved
     * to higher indices and the bits from <em>fromIdx</em> (inclusive) to <em>fromIdx + n</em> (exclusive)
     * will be cleared.
     * @param fromIdx only bits from {@code fromIdx} (inclusive) to the bit-length of this set will be shifted.
     * @param n number of bits to shift
     */
    public void shiftLeft(int fromIdx, int n) {
        int len = bits.length();
        if (n == 0 || fromIdx >= len)
            return;
        for (int i = len - 1; i >= fromIdx; i--)
            bits.set(i + n, bits.get(i));
        bits.clear(fromIdx, fromIdx + n);
    }

    /**
     * Shift the bits representing this attribute set to the right; that is, bits will be moved
     * to lower indices and previous bit-length of this set will be reduced by <em>n</em>.
     * @param fromIdx only bits from {@code fromIdx} (inclusive) to the bit-length of this set will be shifted.
     * @param n number of bits to shift
     */
    public void shiftRight(int fromIdx, int n) {
        int len = bits.length();
        if (n == 0 || fromIdx >= len)
            return;
        if (n >= len - fromIdx) {   // all bits shifted away
            bits.clear(fromIdx, len);
            return;
        }
        for (int i = fromIdx + n; i < len; i++)
            bits.set(i - n, bits.get(i));
        bits.clear(len - n, len);
    }

    /** Return a new attribute set from a byte array. */
    public static AttributeSet fromByteArray(byte[] bytes) {
        AttributeSet as = new AttributeSet();
        as.bits = BitSet.valueOf(bytes);
        return as;
    }
}
