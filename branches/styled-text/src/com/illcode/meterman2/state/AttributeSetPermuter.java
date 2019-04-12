package com.illcode.meterman2.state;

import com.illcode.meterman2.AttributeSet;

import java.util.List;

/**
 * When a game is loaded, we may need to permute the indices of the attributes
 * in restored attribute sets so that they match current attribute indices.
 */
public final class AttributeSetPermuter
{
    private int n;  // number of old attributes
    private int[] table;  // permutation table
    private boolean needPermute;  // do we actually need to do anything?

    private boolean[] values;  // cache to avoid repeated allocation

    /**
     * Create a new attribute set permuter.
     * @param savedNames list of attribute names when the game was saved
     * @param currentNames list of current attribute names
     */
    public AttributeSetPermuter(List<String> savedNames, List<String> currentNames) {
        n = savedNames.size();
        table = new int[n];
        needPermute = false;
        for (int oldIdx = 0; oldIdx < n; oldIdx++) {
            int newIdx = currentNames.indexOf(savedNames.get(oldIdx));
            table[oldIdx] = newIdx;  // may be -1, if an attribute doesn't exist anymore
            if (oldIdx != newIdx)
                needPermute = true;
        }
        if (needPermute)
            values = new boolean[n];
        else
            table = null; // release memory
    }

    /** Permute an attribute set as necessary. */
    public void permuteAttributeSet(AttributeSet attr) {
        if (!needPermute)
            return;
        // First save all the values in the set,
        for (int i = 0; i < n; i++)
            values[i] = attr.get(i);
        // and then rearrange!
        attr.clear();
        for (int i = 0; i < n; i++) {
            if (values[i] == true) { // if the attr at the old index was set
                int newIdx = table[i];
                if (newIdx != -1)
                    attr.set(newIdx);  // set the attribute at the new index
            }
        }
    }
}
