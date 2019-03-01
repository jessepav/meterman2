package com.illcode.meterman2;

import java.util.Formatter;

/**
 * This class handles registration of actions.
 * @see Action
 */
public final class MMActions
{
    private int numActions;
    private int numSystemActions;

    MMActions() {
        clear();
    }

    /**
     * Register an action.
     * @param templateText the action template text. This may contain printf-style
     *      format string syntax as interpreted by {@link Formatter}.
     * @return a new Action
     */
    public Action registerAction(String templateText) {
        return new Action(numActions++, templateText);
    }

    /** Clear all registered actions. */
    void clear() {
        numActions = numSystemActions = 0;
    }

    /**
     * Indicate that the registration of system actions is finished. A subsequent call to
     * {@link #clearGameActions()} will reset the registration system to the state at the
     * point of this call.
     */
    void markSystemActionsDone() {
        numSystemActions = numActions;
    }

    /**
     * Reset the action registration system to its state at the point when
     * {@link #markSystemActionsDone()} was called.
     */
    void clearGameActions() {
        numActions = numSystemActions;
    }

    /**
     * {@code Action} instances are used to handle the display and processing of entity actions.
     * Internally, they contain an <em>action number</em> that represents the identity of the {@code Action}:
     * two {@code Action}S with the same action number are equal -- in the sense that equals() returns
     * true -- even if their displayed text is different.
     * <p/>
     * Each Action references two strings:
     * <ul>
     *     <li><em>template-text</em>, specified when the Action was registered with the system.
     *     This may contain printf-style format sequences, which are later fixed in place by a
     *     call to {@link #formattedTextCopy(Object...)}</li>
     *     <li><em>fixed-text</em>, a string that, if not-null, will be used in place of the template
     *     text in calls to {@link #getText()}.</li>
     * </ul>
     * This system was designed to accommodate entities such as containers, where the same conceptual
     * action ("Put") may be displayed as "Put Under", "Put On", or "Put In" depending on the nature
     * of the container. Each container would derive an action by code like:
     * <blockquote>
     *    {@code putAction = SystemAction.PUT.formattedTextCopy(preposition) }
     * </blockquote>
     * where the template-text of {@code SystemAction.PUT} is {@code "Put %s"}. In the action-processing
     * code, {@code SystemAction.PUT.equals(putAction) == true}.
     * <p/>
     * The only way to mint an action with a new action number is by a call to {@link MMActions#registerAction}.
     */
    public final static class Action
    {
        private final int actionNo;
        private final String templateText;
        private final String fixedText;

        /**
         * Create a new action with a given action number and template-text. This is used only
         * by {@link MMActions}.
         * @param actionNo action number
         * @param templateText template text
         */
        private Action(int actionNo, String templateText) {
            this(actionNo, templateText, null);
        }

        /** Constructor used by {@link #formattedTextCopy} and {@link #fixedTextCopy}.*/
        private Action(int actionNo, String templateText, String fixedText) {
            this.actionNo = actionNo;
            this.templateText = templateText;
            this.fixedText = fixedText;
        }

        /**
         * Return the text represented by this Action.
         * @return if this Action's fixed-text is not null, then return the fixed-text;
         *         otherwise return the template-text.
         */
        public String getText() {
            return fixedText != null ? fixedText : templateText;
        }

        /**
         * Return a copy of this Action whose fixed text is determined by applying the given arguments
         * to the printf-style format string stored as this Action's template-text.
         * @param args arguments for the printf-style format string
         * @return copy of this Action with its fixed-text set
         */
        public Action formattedTextCopy(Object... args) {
            return new Action(actionNo, templateText, String.format(templateText, args));
        }

        /**
         * Return a copy of this Action whose fixed text is set to a given string.
         * @param text fixed text to use for the returned Action
         * @return copy of this Action with its fixed-text set
         */
        public Action fixedTextCopy(String text) {
            return new Action(actionNo, templateText, text);
        }

        public int hashCode() {
            return actionNo;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            else if (obj == null || !(obj instanceof Action))
                return false;
            else
                return this.actionNo == ((Action)obj).actionNo;
        }

        public String toString() {
            return getText();
        }
    }
}
