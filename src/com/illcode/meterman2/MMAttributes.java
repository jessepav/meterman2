package com.illcode.meterman2;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Represents attributes (efficiently represented boolean flags) of game objects.
 * Games may add their own attributes to our database. To clients of this class, an
 * attribute is an integer; we provide methods to convert an integer to the attribute
 * name and vice versa.
 */
public class MMAttributes
{
    private List<String> attributeNames;

    private int nextAttrNum;
    private int numSystemAttributes;

    public MMAttributes() {
        nextAttrNum = numSystemAttributes = 0;
        attributeNames = new ArrayList<>(32);
    }

    /**
     * Register an attribute.
     * @param name attribute name
     * @return integer attribute number
     */
    public int registerAttribute(String name) {
        attributeNames.add(name);
        return nextAttrNum++;
    }

    /** Clear all registered attributes. */
    public void clear() {
        attributeNames.clear();
        nextAttrNum = numSystemAttributes = 0;
    }

    /**
     * Indicate that the registration of system attributes is finished. A subsequent call to
     * {@link #clearGameAttributes()} will reset the registration system to the state at the
     * point of this call.
     */
    void markSystemAttributesDone() {
        numSystemAttributes = nextAttrNum;
    }

    /**
     * Reset the attribute registration system to its state at the point when
     * {@link #markSystemAttributesDone()} was called.
     */
    void clearGameAttributes() {
        while (nextAttrNum > numSystemAttributes)
            attributeNames.remove(--nextAttrNum);
    }

    /** Return the name for a given attribute number. */
    public String nameForAttribute(int attrNum) {
        if (attrNum < attributeNames.size())
            return attributeNames.get(attrNum);
        else
            return "[invalid attribute number]";
    }

    /** Return the attribute number for a given name, or -1 if no such name exists. */
    public int attributeForName(String name) {
        return attributeNames.indexOf(name);
    }

    /**
     * A set of attributes to be associated with a game object.
     */
    public static class AttributeSet
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
        public AttributeSet copyOf(AttributeSet set) {
            AttributeSet newSet = new AttributeSet();
            newSet.bits = (BitSet) set.bits.clone();
            return newSet;
        }
    }
}
