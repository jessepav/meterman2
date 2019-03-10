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

    MMAttributes() {
        attributeNames = new ArrayList<>(32);
    }

    void dispose() {
        clear();
    }

    /**
     * Register an attribute.
     * @param name attribute name, case insensitive.
     * @return integer attribute number
     */
    public int registerAttribute(String name) {
        attributeNames.add(name.toLowerCase());
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

    /** Get the number of system attributes registered. */
    public int getNumSystemAttributes() {
        return numSystemAttributes;
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

    /** Return the attribute number for a given name (case insensitive), or -1 if no such name exists. */
    public int attributeForName(String name) {
        return attributeNames.indexOf(name.toLowerCase());
    }
}
