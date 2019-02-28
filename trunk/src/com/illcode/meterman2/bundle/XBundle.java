package com.illcode.meterman2.bundle;

import com.illcode.meterman2.text.TextSource;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

    private char escapeChar = '@';
    private int paragraphStyle = PARAGRAPH_BLANK_LINE;
    private static final String INDENT = "    ";

    public XBundle(Document doc) {
        this.doc = doc;
        elementMap = new HashMap<>(200);
        passageMap = new HashMap<>(100);
        populateMaps();
    }

    // Used only for testing
    private XBundle() {
    }

    private void populateMaps() {
        Element root = doc.getRootElement();
        if (!root.getName().equals("xbundle")) {
            logger.warning("Invalid XBundle root element: " + root.getName());
            return;
        }

    }

    public Element getElement(String id) {
        return null;
    }

    public TextSource getPassage(String id) {
        return null;
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

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
        String text = new String(bytes, StandardCharsets.UTF_8);
        XBundle xb = new XBundle();
        System.out.print(xb.formatText(text));
    }
}
