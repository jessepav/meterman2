package com.illcode.meterman2.loader;

import com.illcode.meterman2.AttributeSet;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper for a DOM element that provides a variety of utility methods
 * useful in writing loaders.
 */
public class LoaderHelper
{
    private Element el;

    private LoaderHelper(Element el) {
        this.el = el;
    }

    public static LoaderHelper wrap(Element el) {
        return new LoaderHelper(el);
    }

    /**
     * Returns either the value of the attribute with the given name, or if not present,
     * the trimmed text content of the child element with the given name. If neither are present,
     * returns a default value.
     * @param name name of attribute or child element
     * @param defaultVal default to return if value not present
     * @return text value
     */
    public String getValue(String name, String defaultVal) {
        String val = el.getAttributeValue(name);
        if (val == null)
            val = el.getChildTextTrim(name);
        return val != null ? val : defaultVal;
    }

    /**
     * Returns either the value of the attribute with the given name, or if not present,
     * the text content of the child element with the given name. If neither are present,
     * returns null.
     * @param name name of attribute or child element
     * @return text value
     */
    public String getValue(String name) {
        return getValue(name, null);
    }

    /**
     * Get the int value of an attribute or child element.
     * @param name name of attribute or child element
     * @param defaultVal default value if no such attribute or child is present
     * @return int value
     */
    public int getIntValue(String name, int defaultVal) {
        return Utils.parseInt(getValue(name), defaultVal);
    }

    /**
     * Returns either the list value of the attribute with the given name, or if not present, the list value
     * of the child element with the given name. If neither are present, returns null. The "list value" is
     * defined differently for an attribute and child element:
     * <dl>
     *     <dt>attribute</dt>
     *     <dd>The value of the attribute should be a comma-separated list of the form
     *         <tt>"val1, val2, val3, ..."</tt></dd>
     *     <dt>child element</dt>
     *     <dd>
     *         The values of the children of the child element are gathered and returned as a list.
     *         For example, if <em>name</em> is "equipment", then the list value of
     *         <pre>{@code
     * <equipment>
     *     <item>helmet</item>
     *     <item>sword</item>
     *     <item>cola</item>
     * </equipment>
     *         }</pre>
     *         is <tt>["helmet", "sword", "cola"]</tt>.<br/>
     *         The names of the grandchild elements (in this example <em>item</em>) are not taken into account.
     *     </dd>
     * </dl>
     *
     * @param name name of attribute or child element
     * @return list value
     */
    public List<String> getListValue(String name) {
        final String attrVal = el.getAttributeValue(name);
        if (attrVal != null) {
            final String[] vals = StringUtils.split(attrVal, ", ");
            return Arrays.asList(vals);
        }
        // Okay, let's look at the child.
        final Element child = el.getChild(name);
        if (child == null)
            return Collections.emptyList();
        final List<String> values = new ArrayList<>();
        for (Element grandchild : child.getChildren())
            values.add(grandchild.getValue());
        return values;
    }

    /**
     * Load game attributes from an XML attribute or child element.
     * @param name name of XML attribute or child element
     * @param attributes attribute set
     */
    public void loadAttributes(String name, AttributeSet attributes) {
        final List<String> attributeNames = getListValue(name);
        for (String attrName : attributeNames) {
            int attrNum = Meterman2.attributes.attributeForName(attrName);
            if (attrNum != -1)
                attributes.set(attrNum);
        }
    }
}
