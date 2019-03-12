package com.illcode.meterman2.bundle;

import com.illcode.meterman2.text.ScriptSource;
import com.illcode.meterman2.text.StringSource;
import com.illcode.meterman2.text.TemplateSource;
import com.illcode.meterman2.text.TextSource;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * An {@code XBundle} is a wrapper around a JDOM XML tree, providing access to DOM {@link Element} and
 * {@link TextSource} instances by their {@code id} attributes.
 */
public final class XBundle
{
    /** Constant indicating paragraphs should be separated by a blank line. */
    public static final int PARAGRAPH_BLANK_LINE = 0;

    /** Constant indicating paragraphs should be indicated by an indent. */
    public static final int PARAGRAPH_INDENTED = 1;

    /** Returned if a passage is requested that doesn't exist.*/
    public static final TextSource MISSING_TEXT_SOURCE = new StringSource("[missing]");

    /** Returned if a passage has some sort of error. */
    public static final TextSource ERROR_TEXT_SOURCE = new StringSource("[error]");

    // Used to indicate a passage exists but hasn't yet been parsed.
    private static final TextSource PLACEHOLDER_TEXT_SOURCE = new StringSource("[placeholder]");

    private String name;
    private Path path;  // the path from which we were loaded
    private Document doc;
    private Element root;
    private Map<String,Element> elementMap;
    private Map<String,TextSource> passageMap;

    private char escapeChar = '@';
    private char spaceChar = '\u00AC';
    private int paragraphStyle = PARAGRAPH_BLANK_LINE;
    private String indent = "    ";

    private static SAXBuilder saxBuilder;

    private static SAXBuilder getSAXBuilder() {
        if (saxBuilder == null)
            saxBuilder = new SAXBuilder();
        return saxBuilder;
    }

    private XBundle(Path path) {
        this.path = path;
        elementMap = new HashMap<>(200);
        passageMap = new HashMap<>(100);
    }

    /**
     * Load a new XBundle by reading and parsing an XML document at a given path.
     * <p/>
     * This method along with {@link #reloadBundle(XBundle)} are not thread-safe (they share a common
     * XML parser), and invocations of either must be properly synchronized.
     * @param p path of the XML document, must not be null
     */
    public static XBundle loadFromPath(Path p) {
        XBundle b = new XBundle(p);
        reloadBundle(b);
        return b;
    }

    /**
     * Reload a bundle from its original path.
     * <p/>
     * This method along with {@link #loadFromPath(Path)} are not thread-safe (they share a common
     * XML parser), and invocations of either must be properly synchronized.
     * @param b bundle
     */
    public static void reloadBundle(XBundle b) {
        try {
            Document doc = getSAXBuilder().build(b.path.toFile());
            b.initBundle(doc);
        } catch (JDOMException|IOException ex) {
            logger.log(Level.WARNING, "Exception loading an XBundle from " + b.path.getFileName().toString(), ex);
        }
    }

    private void initBundle(Document doc) {
        this.doc = doc;
        if (!doc.hasRootElement()) {
            logger.warning("XBundle document has no root element.");
            return;
        }
        root = doc.getRootElement();
        if (!root.getName().equals("xbundle")) {
            logger.warning("Invalid XBundle root element: " + root.getName());
            return;
        }
        name = root.getAttributeValue("name");
        if (name == null) {
            logger.warning("XBundle root element has no 'name' attribute.");
            return;
        }
        elementMap.clear();
        passageMap.clear();
        for (Element e : root.getChildren()) {
            String id = e.getAttributeValue("id");
            if (id != null && !id.isEmpty()) {
                elementMap.put(id, e);
                if (e.getName().equals("passage"))
                    passageMap.put(id, PLACEHOLDER_TEXT_SOURCE);
            }
        }
    }

    /** Return the name of this bundle. */
    public String getName() {
        return name;
    }

    /**
     * Return the XML element with the given id attribute, or null if no such element exists.
     */
    public Element getElement(String id) {
        return elementMap.get(id);
    }

    /**
     * Returns a list of elements directly under the XBundle root with the given local name.
     * @param cname local name of children
     * @return matching child elements
     */
    public List<Element> getElements(String cname) {
        if (root == null)
            return Collections.emptyList();
        else
            return root.getChildren(cname);
    }

    /**
     * Return a TextSource of the passage with the given id attribute. If no such
     * passage exists, return {@link #MISSING_TEXT_SOURCE}.
     */
    public TextSource getPassage(String id) {
        TextSource source = passageMap.get(id);
        if (source == null) {
            source = MISSING_TEXT_SOURCE;
        } else if (source == PLACEHOLDER_TEXT_SOURCE) {
            // We only construct the actual source the first time the passage is requested.
            source = elementTextSource(elementMap.get(id));
            passageMap.put(id, source);  // and save it for next time
        }
        return source;
    }

    /**
     * Return an appropriate {@link TextSource} implementation for a given element. If an element
     * tries to be both a template and a script (by including both attributes), it will
     * be treated as a template.
     * <p/>
     * If the element has a <em>passageId</em> attribute, then the TextSource of the referenced passage
     * will be returned in its place.
     * @param e XML Element
     * @return an appropriate TextSource implementation, or {@link #ERROR_TEXT_SOURCE}.
     * @see #getElementIdAttribute(Element)
     */
    public TextSource elementTextSource(final Element e) {
        if (e == null)
            return ERROR_TEXT_SOURCE;

        final String passageId = e.getAttributeValue("passageId");
        if (passageId != null)
            return getPassage(passageId);

        if (isTemplateElement(e)) {
            final String id = getElementIdAttribute(e);
            if (id == null) return ERROR_TEXT_SOURCE;
            return new TemplateSource(id, e.getText(), this);
        } else if (isScriptElement(e)) {
            final String id = getElementIdAttribute(e);
            if (id == null) return ERROR_TEXT_SOURCE;
            return new ScriptSource(id, e.getText(), this);
        } else { // a normal text passage
            return new StringSource(formatText(e.getText()));
        }
    }

    /** A template text source element must have a "template" attribute. */
    private static boolean isTemplateElement(final Element e) {
        final Attribute templateAttr = e.getAttribute("template");
        // in the future we can check for the actual value of the template attribute, like "ftl"
        return templateAttr != null;
    }

    /** A script text source element must have a "script" attribute. */
    private static boolean isScriptElement(final Element e) {
        final Attribute scriptAttr = e.getAttribute("script");
        // in the future we can check for the actual value of the script attribute, like "bsh"
        return scriptAttr != null;
    }

    /**
     * Search from the given element up through its parents for the first element that has
     * an 'id' attribute. If the given element does not itself have an 'id' attribute, then
     * the return value will be a synthesized ID of the form:
     * <blockquote>
     *     {@code <parent id value>:<given element name>}
     * </blockquote>
     * For instance, if we start the search at a <em>description</em> element that does not
     * have an 'id' attribute, but its parent, an <em>entity</em> element, has an id of "farmer",
     * the returned ID will be "farmer:description".
     * @param e element at which to start our search
     * @return the actual ID of <em>e</em>, a synthesized ID element as described above, or null
     *          if no element up through the root has an 'id' attribute.
     */
    public static String getElementIdAttribute(Element e) {
        String id = e.getAttributeValue("id");
        if (id != null)
            return id;
        // Otherwise, we have to search and synthesize
        Element parent = e.getParentElement();
        while (parent != null) {
            id = parent.getAttributeValue("id");
            if (id != null)
                return id + ":" + e.getName();
            parent = parent.getParentElement();
        }
        return null;
    }

    /** Get the escape character (by default '@') used to start escape sequences. */
    public char getEscapeChar() {
        return escapeChar;
    }

    /** Set the escape character used to start escape sequences. */
    public void setEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
    }

    /** Get the character used as a single-character escape sequence for a space. */
    public char getSpaceChar() {
        return spaceChar;
    }

    /** Set the character used as a single-character escape sequence for a space. */
    public void setSpaceChar(char spaceChar) {
        this.spaceChar = spaceChar;
    }

    /** Get the number of spaces that will be used to represent tabs and indents. */
    public int getIndentLength() {
        return indent.length();
    }

    /** Set the number of spaces that will be used to represent tabs and indents. */
    public void setIndentLength(int len) {
        indent = StringUtils.repeat(' ', len);
    }

    /**
     * Get the paragraph style used by the <tt>@p</tt> escape sequence.
     * @return paragraph style
     * @see #PARAGRAPH_BLANK_LINE
     * @see #PARAGRAPH_INDENTED
     */
    public int getParagraphStyle() {
        return paragraphStyle;
    }

    /**
     * Set the paragraph style used by the <tt>@p</tt> escape sequence.
     * @param paragraphStyle paragraph style
     * @see #PARAGRAPH_BLANK_LINE
     * @see #PARAGRAPH_INDENTED
     */
    public void setParagraphStyle(int paragraphStyle) {
        this.paragraphStyle = paragraphStyle;
    }

    // @formatter:off
    /**
     * Expand embedded escape sequences in input text.
     * <p/>
     * Note that in the below table of escape sequences, we show the default escape character '@', but
     * it may be changed by {@link #setEscapeChar(char)}; also, the single-character hard space escape
     * is shown as its default, '¬' (U+00AC), but that too can be changed via a call to {@link #setSpaceChar(char)}.
     * <p/>
     * The escape sequences are:
     * <table border="1">
     *     <tr>
     *         <th>Escape Sequence</th><th>Output</th>
     *     </tr>
     *     <tr>
     *         <td>@n</td><td>newline</td>
     *     </tr>
     *     <tr>
     *         <td>@p</td><td>Paragraph. Expands either to two newlines or to a newline
     *                  and an indent, {@link #setParagraphStyle depending on the style} of the text display.</td>
     *     </tr>
     *     <tr>
     *         <td>@t</td><td>"Tab" (actually just expands to {@link #getIndentLength indentLength} spaces)</td>
     *     </tr>
     *     <tr>
     *         <td>¬</td><td>(U+00AC by default). A space. Even though the default character isn't
     *                the easiest to type, it lets one align ASCII drawings or tables properly on the
     *                screen. Anyway, I hope game authors won't need to use hard spaces too often.</td>
     *     </tr>
     *     <tr>
     *         <td>@_</td><td>Also a space, easier to type</td>
     *     </tr>
     *     <tr>
     *         <td>@¬</td><td>the space character itself (U+00AC by default)</td>
     *     </tr>
     *     <tr>
     *         <td>@@</td><td>The literal escape character (in this case '@').</td>
     *     </tr>
     * </table>
     * An escape character followed by anything else (or followed by nothing, if it's the last character
     * in the input) won't be interpreted specially, and will be output as itself.
     * <p/>
     * NOTE: All whitespace after a @p or @n escape sequence is gobbled up, to prevent extraneous spaces
     * from appearing at the start of lines in normalized text.
     * @param text text to unescape
     * @return unescaped text
     */
    // @formatter:on
    public String unescapeText(final String text) {
        final int len = text.length();
        if (len < 2)
            return text;
        final StringBuilder sb = new StringBuilder(text.length() + 30);
        int pos = 0;
        boolean eatingWhitespace = false;
        while (pos < len) {
            final char c = text.charAt(pos);
            if (eatingWhitespace) {
                if (Character.isWhitespace(c)) {
                    pos++;
                    continue;
                } else {
                    eatingWhitespace = false;
                }
            }
            if (c == escapeChar && pos != len-1) {          // don't unescape the last character
                final char nextChar = text.charAt(pos+1);   // in the input (character at len-1)
                if (nextChar == 'n') {
                    sb.append('\n');
                    eatingWhitespace = true;
                } else if (nextChar == 'p') {
                    switch (paragraphStyle) {
                    case PARAGRAPH_INDENTED:
                        sb.append('\n').append(indent);
                        break;
                    default:
                        sb.append("\n\n");
                        break;
                    }
                    eatingWhitespace = true;
                } else if (nextChar == 't') {
                    sb.append(indent);
                } else if (nextChar == '_' ) {
                    sb.append(' ');
                } else if (nextChar == spaceChar) {
                    sb.append(spaceChar);
                } else if (nextChar == escapeChar) {
                    sb.append(escapeChar);
                } else {  // not a special escape sequence
                    sb.append(c).append(nextChar);
                }
                pos++;  // swallow the next character as well
            } else if (c == spaceChar) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
            pos++;
        }
        return sb.toString();
    }

    /**
     * Normalize and expand escape sequences in text, according to the settings of this XBundle.
     * @param text text to format
     * @return formatted text
     */
    public String formatText(final String text) {
        return unescapeText(StringUtils.normalizeSpace(text));
    }

    /*
    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
        String text = new String(bytes, StandardCharsets.UTF_8);
        XBundle xb = new XBundle();
        System.out.print(xb.formatText(text));
    }
    */
}
