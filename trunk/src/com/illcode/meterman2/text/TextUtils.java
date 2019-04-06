package com.illcode.meterman2.text;

public final class TextUtils
{
    /**
     * Wraps text to a specified maximum line length, respecting newlines and spaces.
     * It does not perform any trimming or normalization.
     * @param text text to wrap
     * @param maxLen maximum line length. If <= 0, original text is returned.
     * @return wrapped text, or <tt>null</tt> if input is null
     */
    public static String wrapText(String text, int maxLen) {
        if (text == null || maxLen <= 0)
            return text;
        final int inputLength = text.length();
        final StringBuilder sb = new StringBuilder(inputLength + (inputLength/maxLen) + 10);
        for (int offset = 0; offset < inputLength;) {
            int nextNewline = indexOf(text, '\n', offset, inputLength);
            if (nextNewline == -1)
                nextNewline = inputLength;
            while (nextNewline > offset + maxLen) {
                // we poke one character beyond the maxLen here looking for a space, but that's okay
                // because the condition of the while loop guarantees that inputLength > offset+maxLen.
                final int lastSpace = lastIndexOf(text, ' ', offset + maxLen, offset);
                if (lastSpace != -1) {
                    sb.append(text, offset, lastSpace).append('\n');
                    offset = lastSpace + 1;
                } else {  // this is a long word
                    final int breakOffset = offset + maxLen - 1;
                    sb.append(text, offset, breakOffset).append('-').append('\n');
                    offset = breakOffset;
                }
            }
            sb.append(text, offset, nextNewline);
            if (nextNewline != inputLength)
                sb.append('\n');
            offset = nextNewline + 1;
        }
        return sb.toString();
    }

    /**
     * Return the first index of a character in a CharSequence.
     * @param cs CharSequence to search
     * @param c character to search for
     * @param start start index (inclusive) in <tt>cs</tt> from which we search forwards
     * @param limit limit index (exclusive) of our search
     * @return index of <tt>c</tt>, or -1 if not found before the limit
     */
    private static int indexOf(CharSequence cs, char c, int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (cs.charAt(i) == c)
                return i;
        }
        return -1;
    }

    /**
     * Return the last index of a character in a CharSequence.
     * @param cs CharSequence to search
     * @param c character to search for
     * @param start start index (inclusive) in <tt>cs</tt> from which we search backwards
     * @param limit limit index (<b>also inclusive</b>) of our search
     * @return index of <tt>c</tt>, or -1 if not found after the limit
     */
    private static int lastIndexOf(CharSequence cs, char c, int start, int limit) {
        for (int i = start; i > limit; i--) {
            if (cs.charAt(i) == c)
                return i;
        }
        return -1;
    }
}
