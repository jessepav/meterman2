package com.illcode.meterman2.model;

import com.illcode.meterman2.GameUtils;
import com.illcode.meterman2.MMActions;
import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.model.TopicMap.Topic;
import com.illcode.meterman2.text.TextSource;

import java.util.*;

import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.ui;

/**
 * Support class for interacting entities.
 */
public final class InteractSupport
{
    private Entity e;
    private TopicMap topicMap;
    private Collection<Topic> currentTopics;
    private Topic otherTopic;
    private MMScript.ScriptedMethod beginInteractMethod, topicChosenMethod, interactOtherMethod;
    private MMActions.Action interactAction;
    private TextSource promptMessage, noTopicsMessage;
    private InteractHandler interactHandler;

    private List<Topic> assembledTopics; // for use with assembleTopicList() to avoid allocation

    /**
     * Create a interact-support instance for the given entity.
     * @param e entity bound to this support object
     */
    public InteractSupport(Entity e) {
        this.e = e;
        currentTopics = new LinkedHashSet<>();
        assembledTopics = new ArrayList<>();
        interactAction = SystemActions.INTERACT;
    }

    /**
     * Return the topic map being used.
     */
    public TopicMap getTopicMap() {
        return topicMap;
    }

    /**
     * Set the topic map to be used.
     */
    public void setTopicMap(TopicMap topicMap) {
        this.topicMap = topicMap;
    }

    /**
     * Add a topic to our list of current topics.
     * @param id topic ID, as found in the topic map.
     */
    public void addTopic(String id) {
        if (topicMap == null)
            return;
        final Topic t = topicMap.getTopic(id);
        if (t != null)
            currentTopics.add(t);
    }

    /**
     * Remove a topic from our list of current topics.
     * @param id topic ID, as found in the topic map.
     */
    public void removeTopic(String id) {
        if (topicMap == null)
            return;
        final Topic t = topicMap.getTopic(id);
        if (t != null)
            currentTopics.remove(t);
    }

    /**
     * Clear all current topics.
     */
    public void clearTopics() {
        currentTopics.clear();
    }

    /**
     * Set the label to be used to display the "Other Topic"
     * @param label label for "Other Topic"; if null, no "Other Topic" will be shown.
     */
    public void setOtherTopicLabel(String label) {
        if (label == null)
            otherTopic = null;
        else
            otherTopic = new Topic(TopicMap.OTHER_TOPIC_ID, label);
    }

    /** Return the possibly customized interact action being used. */
    public MMActions.Action getInteractAction() {
        return interactAction;
    }

    /** Set the text of this interact-support's Interact action. */
    public void setInteractActionText(String text) {
        if (text != null && !text.isEmpty())
            interactAction = SystemActions.INTERACT.fixedTextCopy(text);
    }

    /**
     * Set the message to be displayed when there are no topics for interaction.
     * @param noTopicsMessage text source to display. Its {@link TextSource#getTextWithArgs(Object...)} will
     * be called with the entity's defName as a parameter.
     */
    public void setNoTopicsMessage(TextSource noTopicsMessage) {
        this.noTopicsMessage = noTopicsMessage;
    }

    /**
     * Set the message to be displayed as a prompt for interaction.
     * @param promptMessage text source to display. Its {@link TextSource#getTextWithArgs(Object...)} will
     * be called with the entity's defName as a parameter.
     */
    public void setPromptMessage(TextSource promptMessage) {
        this.promptMessage = promptMessage;
    }

    /** Get our interact-handler, or null if none set. */
    public InteractHandler getInteractHandler() {
        return interactHandler;
    }

    /** Set the interact-handler to be used for out-of-topic-map behavior, if no relevant scripted method
     *  is defined. */
    public void setInteractHandler(InteractHandler interactHandler) {
        this.interactHandler = interactHandler;
    }

    /**
     * Set scripted methods to be used by this interact-support.
     * <p/>
     * It looks for methods with these names (and implicit signatures) in the passed method map:
     * <pre>{@code
     *    void beginInteract(Entity e)
     *    boolean topicChosen(Entity e, TopicMap.Topic t)
     *    void interactOther(Entity e, String topic)
     * }</pre>
     * and if present will call them in place of the corresponding {@link InteractSupport.InteractHandler} methods when
     * going through the interact process.
     * @param methodMap map from method name to scripted method. If null, all of our scripted methods
     * will be cleared
     */
    public void setScriptedMethods(Map<String,MMScript.ScriptedMethod> methodMap) {
        if (methodMap != null) {
            beginInteractMethod = methodMap.get("beginInteract");
            topicChosenMethod = methodMap.get("topicChosen");
            interactOtherMethod = methodMap.get("interactOther");
        } else {
            beginInteractMethod = null;
            topicChosenMethod = null;
            interactOtherMethod = null;
        }
    }

    /** Entry point to the interaction system, called when the user selects
     *  the Interact action on the associated entity. */
    public void interact() {
        beginInteract();
        final List<Topic> topics = assembleTopicList();
        if (topics.isEmpty()) {
            gm.println(getNoTopicsMessage());
        } else {
            Topic t;
            if (topics.size() == 1 && topics.get(0).getId().equals(TopicMap.GREETING_TOPIC_ID))
                t = topics.get(0);
            else
                t = ui.showListDialog(interactAction.getText(), getPromptMessage(), topics, true);
            if (t == null)
                return;
            if (t.getId() == TopicMap.OTHER_TOPIC_ID) {
                String s = ui.showPromptDialog(interactAction.getText(), t.getLabel(), "Topic:", "");
                interactOther(s);
            } else {
                if (topicChosen(t))
                    return;  // it was handled by a script or InteractHandler
                // Now the normal conversation cycle.
                GameUtils.pushBinding("entity", e);
                if (t.isDialogTopic())
                    t.showDialog();
                else
                    gm.println(t.getText());
                GameUtils.popBinding("entity");
                for (String topicId : t.getRemoveTopics()) {
                    if (topicId.equals("all")) {
                        clearTopics();
                        break;
                    } else {
                        removeTopic(topicId);
                    }
                }
                for (String topicId : t.getAddTopics())
                    addTopic(topicId);
            }
        }
    }

    private String getNoTopicsMessage() {
        final TextSource ts = noTopicsMessage != null ? noTopicsMessage : bundles.getPassage("no-interact-topics-message");
        return ts.getTextWithArgs(e.getDefName());
    }

    private String getPromptMessage() {
        final TextSource ts = promptMessage != null ? promptMessage : bundles.getPassage("interact-prompt-message");
        return ts.getTextWithArgs(e.getDefName());
    }

    /**
     * Return a state object that can later be used to restore this interact-support's state.
     * <p/>
     * The state of a interact-support instance comprises a collection of current topics IDs.
     */
    public Object getState() {
        if (currentTopics.isEmpty())
            return null;
        final String[] currentTopicIds = new String[currentTopics.size()];
        int idx = 0;
        for (Topic t : currentTopics)
            currentTopicIds[idx++] = t.getId();
        return currentTopicIds;
    }

    /**
     * Restore state from a previously saved state object.
     * <p/>
     * The state of a interact-support instance comprises a collection of current topics IDs.
     * @param state state object, as returned by {@code getState()}.
     */
    public void restoreState(Object state) {
        clearTopics();
        if (state == null || topicMap == null)
            return;
        final String[] currentTopicIds = (String[]) state;
        for (String id : currentTopicIds) {
            final Topic t = topicMap.getTopic(id);
            if (t != null)
                currentTopics.add(t);
        }
    }

    private List<Topic> assembleTopicList() {
        assembledTopics.clear();
        assembledTopics.addAll(currentTopics);
        if (otherTopic != null)
            assembledTopics.add(otherTopic);
        return assembledTopics;
    }

    private void beginInteract() {
        if (beginInteractMethod != null)
            beginInteractMethod.invoke(e);
        else if (interactHandler != null)
            interactHandler.beginInteract(e);
        // otherwise do nothing
    }

    private boolean topicChosen(Topic t) {
        if (topicChosenMethod != null)
            return topicChosenMethod.invokeWithResultOrError(Boolean.class, false, e, t);
        else if (interactHandler != null)
            return interactHandler.topicChosen(e, t);
        else
            return false;
    }

    private void interactOther(String topic) {
        if (interactOtherMethod != null)
            interactOtherMethod.invoke(e, topic);
        else if (interactHandler != null)
            interactHandler.interactOther(e, topic);
        // otherwise do nothing
    }

    /**
     * Interface for classes that handles interact behavior outside of the normal topic-map flow.
     */
    public static interface InteractHandler
    {
        /**
         * Called at the beginning of the interact action process, before choice lists are constructed. A
         * class may implement this to, say, add or remove topics based on global game state.
         * @param e entity with which the user is interacting
         */
        void beginInteract(Entity e);

        /**
         * Called when the user has selected one of the active topics.
         * @param t topic chosen
         * @param e entity with which the user is interacting
         * @return false to show the topic text and process add- and remove-topics as usual,
         *         true if the implementation has handled this topic itself and wants to skip
         *         normal processing.
         */
        boolean topicChosen(Entity e, Topic t);

        /**
         * If the user chooses "Other topic" he will be prompted to type something in,
         * and this method will be called with the typed text.
         * @param e entity with which the user is interacting
         * @param topic user-input topic text
         */
        void interactOther(Entity e, String topic);
    }
}
