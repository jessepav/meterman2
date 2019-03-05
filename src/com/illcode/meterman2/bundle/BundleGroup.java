package com.illcode.meterman2.bundle;

import com.illcode.meterman2.text.TextSource;
import org.jdom2.Element;

import java.util.LinkedList;

/**
 * A BundleGroup maintains a list of {@code XBundle}S and supports operations that
 * query each in order, as though the group were one big {@code XBundle}.
 * <p/>
 * Note that these group query methods only work for top-level elements and passages,
 * i.e. those directly under the {@code <xbundle>} element in the XML source.
 */
public final class BundleGroup
{
    private LinkedList<XBundle> bundles;

    /** Construct an empty BundleGroup. */
    public BundleGroup() {
        bundles = new LinkedList<>();
    }

    /**
     * Add a bundle to the head of our bundle list.
     */
    public void addFirst(XBundle bundle) {
        bundles.addFirst(bundle);
    }

    /**
     * Add a bundle to the tail of our bundle list.
     */
    public void addLast(XBundle bundle) {
        bundles.addLast(bundle);
    }

    /**
     * Search our bundle list from head to tail for an element.
     * @param id id of the element
     * @return element with the given id, or null if not found in any of our bundles.
     */
    public Element getElement(String id) {
        Element e = null;
        for (XBundle b : bundles) {
            e = b.getElement(id);
            if (e != null)
                break;
        }
        return e;
    }

    /**
     * Search our bundle list from head to tail for a passage.
     * @param id id of the passage element
     * @return TextSource representing the text contained in the passage, or {@link XBundle#MISSING_TEXT_SOURCE}
     *         if no such passage is found.
     */
    public TextSource getPassage(String id) {
        TextSource ts = XBundle.MISSING_TEXT_SOURCE;
        for (XBundle b : bundles) {
            ts = b.getPassage(id);
            if (ts != XBundle.MISSING_TEXT_SOURCE)
                break;
        }
        return ts;
    }
}
