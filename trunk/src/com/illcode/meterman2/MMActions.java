package com.illcode.meterman2;

import org.apache.commons.lang3.tuple.Pair;
import org.mini2Dx.gdx.utils.IntMap;
import org.mini2Dx.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * This class handles registration of actions.
 * @see Action
 */
public final class MMActions
{
    private BitSet actionNumSet;  // all registered action numbers
    private BitSet sysActionNumSet; // registered system action numbers

    private IntMap<Pair<Action,String>> sysSavedActionTextMap;  // to save the original templateText of system actions
    private IntMap<String> actionNumNameMap; // quick lookup of name by action #
    private ObjectMap<String,Action> actionNameMap; // to lookup actions by name

    private List<Action> systemActions;
    private List<Action> gameActions;

    MMActions() {
        actionNumSet = new BitSet();
        sysActionNumSet = new BitSet();
        sysSavedActionTextMap = new IntMap<>(32);
        actionNameMap = new ObjectMap<>(48);
        actionNumNameMap = new IntMap<>(48);
        systemActions = new ArrayList<>(32);
        gameActions = new ArrayList<>(16);
    }

    void dispose() {
        clear();
        actionNumSet = null;
        sysActionNumSet = null;
        sysSavedActionTextMap = null;
        actionNumNameMap = null;
        actionNameMap = null;
        systemActions = null;
        gameActions = null;
    }

    /**
     * Register a system action.
     * @param name unique logical name for the action
     * @param templateText the action template text. This may contain printf-style
     *      format string syntax as interpreted by {@link java.util.Formatter}.
     * @return a new Action
     */
    public Action registerSystemAction(String name, String templateText) {
        Action a = actionNameMap.get(name);
        if (a == null) {
            final int n = actionNumSet.nextClearBit(0);
            actionNumSet.set(n);
            sysActionNumSet.set(n);
            a = new Action(n, name, templateText);
            sysSavedActionTextMap.put(n, Pair.of(a, templateText));
            actionNameMap.put(name, a);
            actionNumNameMap.put(n, name);
            systemActions.add(a);
        }
        return a;
    }

    /** Deregister a system action. */
    public void deregisterSystemAction(Action a) {
        final int n = a.actionNo;
        if (sysActionNumSet.get(n)) {  // only release system actions
            sysSavedActionTextMap.remove(n);
            actionNameMap.remove(a.getName());
            actionNumNameMap.remove(n);
            sysActionNumSet.clear(n);
            actionNumSet.clear(n);
            systemActions.remove(a);
        }
    }

    /**
     * Register a game action.
     * @param name unique logical name for the action
     * @param templateText the action template text. This may contain printf-style
     *      format string syntax as interpreted by {@link java.util.Formatter}.
     * @return a new Action
     */
    public Action registerAction(String name, String templateText) {
        Action a = actionNameMap.get(name);
        if (a == null) {
            final int n = actionNumSet.nextClearBit(0);
            actionNumSet.set(n);
            a = new Action(n, name, templateText);
            actionNameMap.put(name, a);
            actionNumNameMap.put(n, name);
            gameActions.add(a);
        }
        return a;
    }

    /** Deregister a game action. */
    public void deregisterAction(Action a) {
        final int n = a.actionNo;
        if (!sysActionNumSet.get(n)) { // don't let a game release a system action
            actionNameMap.remove(a.getName());
            actionNumNameMap.remove(n);
            actionNumSet.clear(n);
            gameActions.remove(a);
        }
    }

    /** Return the action with the given name, or null if none exists. */
    public Action getAction(String name) {
        return actionNameMap.get(name);
    }

    /** Clear all registered actions. */
    void clear() {
        actionNumSet.clear();
        sysActionNumSet.clear();
        actionNameMap.clear();
        actionNumNameMap.clear();
        sysSavedActionTextMap.clear();
        systemActions.clear();
        gameActions.clear();
    }

    /** Clear all game actions, and reset system actions to their original state. */
    void clearGameActions() {
        // First clear all the game actions.
        final int size = actionNumSet.size();
        for (int i = 0; i < size; i++) {
            if (actionNumSet.get(i)) {
                if (!sysActionNumSet.get(i)) { // a game action
                    actionNameMap.remove(actionNumNameMap.get(i));
                    actionNumNameMap.remove(i);
                    actionNumSet.clear(i);
                } else {  // a system action
                    final Pair<Action,String> pair = sysSavedActionTextMap.get(i);
                    final Action a = pair.getLeft();
                    final String templateText = pair.getRight();
                    a.setTemplateText(templateText);
                    a.setFixedText(null);
                }
            }
        }
        gameActions.clear();
    }

    /** Return a list of registered system actions. This list is "live" in that it will reflect subsequent
     *  calls to {@code registerSystemAction} and {@code deregisterSystemAction} (and thus you should not
     *  call either of those methods while iterating over this list). */
    public List<Action> getSystemActions() {
        return systemActions;
    }

    /** Return a list of registered game actions.  This list is "live" in that it will reflect subsequent
     *  calls to {@code registerAction} and {@code deregisterAction} (and thus you should not call either
     *  of those methods while iterating over this list). */
    public List<Action> getGameActions() {
        return gameActions;
    }

    /** Returns true if the given action is a system action. */
    public boolean isSystemAction(Action a) {
        return sysActionNumSet.get(a.actionNo);
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
     * The only way to mint an action with a new action number is by a call to {@link MMActions#registerSystemAction}.
     */
    public final static class Action
    {
        private final String name;
        private final int actionNo;
        private String templateText;
        private String fixedText;

        /**
         * Create a new action with a given action number and template-text. This is used only
         * by {@link MMActions}.
         * @param actionNo action number
         * @param name
         * @param templateText template text
         */
        private Action(int actionNo, String name, String templateText) {
            this(actionNo, name, templateText, null);
        }

        private Action(int actionNo, String name, String templateText, String fixedText) {
            this.actionNo = actionNo;
            this.name = name;
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

        public String getName() {
            return name;
        }

        public String getTemplateText() {
            return templateText;
        }

        public void setTemplateText(String templateText) {
            this.templateText = templateText;
        }

        public String getFixedText() {
            return fixedText;
        }

        public void setFixedText(String fixedText) {
            this.fixedText = fixedText;
        }

        /**
         * Return a copy of this Action whose fixed text is determined by applying the given arguments
         * to the printf-style format string stored as this Action's template-text.
         * @param args arguments for the printf-style format string
         * @return copy of this Action with its fixed-text set
         */
        public Action formattedTextCopy(Object... args) {
            return new Action(actionNo, name, templateText, String.format(templateText, args));
        }

        /**
         * Return a copy of this Action whose fixed text is set to a given string.
         * @param text fixed text to use for the returned Action
         * @return copy of this Action with its fixed-text set
         */
        public Action fixedTextCopy(String text) {
            return new Action(actionNo, name, templateText, text);
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
