package com.illcode.meterman2.model;

import com.illcode.meterman2.MMScript;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.SystemMessages;
import com.illcode.meterman2.model.TopicMap.Topic;

import java.util.*;

import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.ui;

public class TalkSupport
{
    protected Talker talker;

    protected TopicMap topicMap;
    protected Collection<Topic> currentTopics;
    protected Topic otherTopic;
    protected boolean checkOtherTopic;

    protected List<Topic> assembledTopics; // for use with assembleTopicList() to avoid allocation

    protected MMScript.ScriptedMethod topicChosenMethod, otherTopicLabelMethod, talkOtherMethod;

    /**
     * Create a talk-support instance for the given talker.
     * @param talker the talker who we're supporting
     */
    public TalkSupport(Talker talker) {
        this.talker = talker;
        currentTopics = new LinkedHashSet<>();
        checkOtherTopic = true;
        assembledTopics = new ArrayList<>();
    }

    /**
     * Return the topic map being used by this Talker.
     */
    public TopicMap getTopicMap() {
        return topicMap;
    }

    /**
     * Set the topic map being used by this Talker.
     */
    public void setTopicMap(TopicMap topicMap) {
        this.topicMap = topicMap;
    }

    /**
     * Entry point to the conversation system, called by the game engine when the user
     * selects the Talk action on this entity.
     */
    public void talk() {
        final List<Topic> topics = assembleTopicList();
        final Entity e = talker.getTalkerEntity();
        if (topics.isEmpty()) {
            gm.println(bundles.getPassage(SystemMessages.NO_TALK_TOPICS).getTextWithArgs(e.getDefName()));
        } else {
            Topic t = ui.showListDialog(SystemActions.TALK.getText(), bundles.getPassage(SystemMessages.TALK_PROMPT).getText(),
                topics, true);
            if (t != null) {
                // TODO: TalkSupport
                // check for other topic and call talkOther()
                // check if talker.topicChosen() returns true
                // run conversation cycle
            }
        }
    }

    /**
     * Add a topic to our list of current topics.
     * @param id topic ID, as found in the topic map.
     */
    public void addTopic(String id) {
        final Topic t = topicMap.getTopic(id);
        if (t != null)
            currentTopics.add(t);
    }

    /**
     * Remove a topic from our list of current topics.
     * @param id topic ID, as found in the topic map.
     */
    public void removeTopic(String id) {
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
     * Set scripted methods to be used by this talk-support.
     * <p/>
     * It looks for methods with these names (and implicit signatures) in the passed method map:
     * <pre>{@code
     *    boolean topicChosen(Talker talker, TopicMap.Topic t)
     *    String getOtherTopicLabel(Talker talker)
     *    void talkOther(Talker talker, String topic)
     * }</pre>
     * and if present will call them in place of the corresponding methods of its associated Talker when
     * going through the talk process.
     * @param methodMap map from method name to scripted method. If null, all of our scripted methods
     * will be cleared
     */
    public void setScriptedMethods(Map<String,MMScript.ScriptedMethod> methodMap) {
        if (methodMap != null) {
            topicChosenMethod = methodMap.get("topicChosen");
            otherTopicLabelMethod = methodMap.get("getOtherTopicLabel");
            talkOtherMethod = methodMap.get("talkOther");
        } else {
            topicChosenMethod = null;
            otherTopicLabelMethod = null;
            talkOtherMethod = null;
        }
    }

    protected List<Topic> assembleTopicList() {
        if (checkOtherTopic) {
            final String otherLabel = getOtherTopicLabel();
            if (otherLabel != null)
                otherTopic = new Topic(TopicMap.OTHER_TOPIC_ID, otherLabel);
            checkOtherTopic = false;
        }
        assembledTopics.clear();
        assembledTopics.addAll(currentTopics);
        if (otherTopic != null)
            assembledTopics.add(otherTopic);
        return assembledTopics;
    }

    protected boolean topicChosen(Topic t) {
        return topicChosenMethod != null ?
            topicChosenMethod.invokeWithResultOrError(Boolean.class, false, talker, t) :
            talker.topicChosen(t);
    }

    protected String getOtherTopicLabel() {
        return otherTopicLabelMethod != null ?
            otherTopicLabelMethod.invokeWithResultOrError(String.class, null, talker) :
            talker.getOtherTopicLabel();
    }

    protected void talkOther(String topic) {
        if (talkOtherMethod != null)
            talkOtherMethod.invoke(talker, topic);
        else
            talker.talkOther(topic);
    }
}
