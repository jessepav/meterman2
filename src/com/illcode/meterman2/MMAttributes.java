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
    private BitSet attributeSet;

    private List<String> attributeNames;

    private int nextAttrNum;
    private int numSystemAttributes;

    public MMAttributes() {
        attributeSet = new BitSet();
        attributeNames = new ArrayList<>(32);
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

    }

}
