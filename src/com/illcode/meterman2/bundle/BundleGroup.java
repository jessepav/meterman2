package com.illcode.meterman2.bundle;

import com.illcode.meterman2.text.TextSource;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import java.util.*;

/**
 * A BundleGroup maintains a list of {@code XBundle}S and supports operations that
 * query each in order for elements and passages with a specific ID.
 * <p/>
 * Note that these group query methods only work for top-level elements and passages,
 * i.e. those directly under the {@code <xbundle>} element in the XML source.
 */
public final class BundleGroup
{
    private LinkedList<XBundle> bundles;
    private List<XBundle> systemBundles;

    /** Construct an empty BundleGroup. */
    public BundleGroup() {
        bundles = new LinkedList<>();
        systemBundles = Collections.emptyList();
    }

    public void dispose() {
        bundles = null;
        systemBundles = null;
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
     * Remove a bundle from our list.
     */
    public void remove(XBundle bundle) {
        bundles.remove(bundle);
    }

    /**
     * Clear our bundle list.
     */
    public void clear() {
        bundles.clear();
    }

    /** Return the bundle in our list with the given name, or null if not found. */
    public XBundle getBundleWithName(String name) {
        for (XBundle bundle : bundles) {
            if (StringUtils.equals(bundle.getName(), name))
                return bundle;
        }
        return null;
    }

    /**
     * Set the "system bundles" of this group. When {@code clearGameBundles()} is called, our bundle
     * list will be restored to the bundles passed to this method.
     * @param bundles system bundles
     */
    public void setSystemBundles(XBundle... bundles) {
        if (bundles == null)
            systemBundles = Collections.emptyList();
        else
            systemBundles = Arrays.asList(bundles);
    }

    /**
     * Restore our bundle list to the bundles passed to {@code setSystemBundles()}.
     */
    public void clearGameBundles() {
        bundles = new LinkedList<>(systemBundles);
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
