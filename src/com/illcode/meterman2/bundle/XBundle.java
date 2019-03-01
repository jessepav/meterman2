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
import java.util.HashMap;
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

    private Document doc;
    private Map<String,Element> elementMap;
    private Map<String,TextSource> passageMap;

    // Used to indicate a passage exists but hasn't yet been parsed.
    private static final TextSource PLACEHOLDER_TEXT_SOURCE = new StringSource("[placeholder]");

    // Returned if a passage is requested that doesn't exist.
    private static final TextSource MISSING_TEXT_SOURCE = new StringSource("[missing]");

    // Returned if a passage has some sort of error.
    private static final TextSource ERROR_TEXT_SOURCE = new StringSource("[error]");

    private char escapeChar = '@';
    private int paragraphStyle = PARAGRAPH_BLANK_LINE;
    private static final String INDENT = "    ";

    /**
     * Construct an XBundle by using an existing JDOM Document.
     * @param doc JDOM Document having a {@code <passage>} element as its root.
     */
    public XBundle(Document doc) {
        this.doc = doc;
        initMaps();
    }

    /**
     * Construct an XBundle by reading and parsing an XML document at a given path.
     * @param p path of the XML document
     */
    public XBundle(Path p) {
        try {
            SAXBuilder sax = new SAXBuilder();
            this.doc = sax.build(p.toFile());
            initMaps();
        } catch (JDOMException|IOException ex) {
            logger.log(Level.WARNING, "Exception constructing an XBundle from Path:", ex);
        }
    }

    // Used only for testing
    private XBundle() {
    }

    private void initMaps() {
        elementMap = new HashMap<>(200);
        passageMap = new HashMap<>(100);
        if (!doc.hasRootElement()) {
            logger.warning("XBundle document has no root element.");
            return;
        }
        Element root = doc.getRootElement();
        if (!root.getName().equals("xbundle")) {
            logger.warning("Invalid XBundle root element: " + root.getName());
            return;
        }
        for (Element e : root.getChildren()) {
            Attribute attr = e.getAttribute("id");
            if (attr != null) {
                String id = attr.getValue();
                elementMap.put(id, e);
                if (e.getName().equals("passage"))
                    passageMap.put(id, PLACEHOLDER_TEXT_SOURCE);
            }
        }
    }

    /**
     * Return an appropriate TextSource implementation for a given passage element.
     * @param e XML Element
     * @return TextSource implementation
     * @see ScriptSource
     * @see StringSource
     * @see TemplateSource
     */
    private TextSource elementTextSource(final Element e) {
        // TODO: flesh out elementTextSource()
        boolean isTemplate = isTemplateElement(e);
        boolean isScript = isScriptElement(e);
        if (isTemplate && isScript) {
            logger.warning("XBundle passage " + e.getName() + " is trying to be a template *and* a script!");
            return ERROR_TEXT_SOURCE;
        }
        if (isTemplate) {
            return PLACEHOLDER_TEXT_SOURCE;
        } else if (isScript) {
            return PLACEHOLDER_TEXT_SOURCE;
        } else { // a normal text passage
            return new StringSource(formatText(e.getText()));
        }
    }

    /** A template text source element must have both "template" and "id" attributes. */
    private boolean isTemplateElement(final Element e) {
        final Attribute templateAttr = e.getAttribute("template");
        final Attribute idAttr = e.getAttribute("id");
        // in the future we can check for the actual value of the template attribute, like "ftl"
        return templateAttr != null && idAttr != null;
    }

    /** A script text source element must have a "script" attribute. */
    private boolean isScriptElement(final Element e) {
        final Attribute scriptAttr = e.getAttribute("script");
        return scriptAttr != null;  // in the future we can check for the actual value, like "bsh"
    }

    /**
     * Return the XML element with the given id attribute, or null if no such element exists.
     */
    public Element getElement(String id) {
        return elementMap.get(id);
    }

    /**
     * Return a TextSource of the passage with the given id attribute.
     */
    public TextSource getPassage(String id) {
        TextSource source = passageMap.get(id);
        if (source == null) {
            source = MISSING_TEXT_SOURCE;
        } else if (source == PLACEHOLDER_TEXT_SOURCE) {
            // We only construct the actual source the first time the passage is requested.
            // The elementMap surely contains such an element because the id was in the passageMap.
            source = elementTextSource(elementMap.get(id));
            passageMap.put(id, source);  // and save it for next time
        }
        return source;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
    }

    public int getParagraphStyle() {
        return paragraphStyle;
    }

    public void setParagraphStyle(int paragraphStyle) {
        this.paragraphStyle = paragraphStyle;
    }

    /**
     * Expand embedded escape sequences in input text.
     * <p/>
     * Note that in the below table of escape sequences, we show the default escape character '@', but
     * it may be changed by {@link #setEscapeChar(char)}.
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
     *                        and an indent, depending on the style of the text display.</td>
     *     </tr>
     *     <tr>
     *         <td>@t</td><td>"Tab" (actually just expands to 4 spaces)</td>
     *     </tr>
     *     <tr>
     *         <td>␣</td><td>(U+2423). A space. Even though the character isn't the easiest
     *                        to type, it lets one align ASCII drawings or tables properly on the screen.
     *                        Anyway, I hope game authors won't need to use hard spaces too often.</td>
     *     </tr>
     *     <tr>
     *         <td>@_</td><td>Also a space, easier to type</td>
     *     </tr>
     *     <tr>
     *         <td>@␣</td><td>The Unicode Open Box character (U+2423), in case someone actually
     *                         wants to use it</td>
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
                        sb.append('\n').append(INDENT);
                        break;
                    default:
                        sb.append("\n\n");
                        break;
                    }
                    eatingWhitespace = true;
                } else if (nextChar == 't') {
                    sb.append(INDENT);
                } else if (nextChar == '_' ) {
                    sb.append(' ');
                } else if (nextChar == '\u2423') {  // the Unicode Open Box character
                    sb.append('\u2423');
                } else if (nextChar == escapeChar) {
                    sb.append(escapeChar);
                } else {  // not a special escape sequence
                    sb.append(c).append(nextChar);
                }
                pos++;  // swallow the next character as well
            } else if (c == '\u2423') {
                sb.append(' ');
            } else {
                sb.append(c);
            }
            pos++;
        }
        return sb.toString();
    }

    /**
     * Normalize and expand escape sequences in text.
     * @param text text to process
     * @return processed text
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
