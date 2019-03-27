package com.illcode.meterman2.model;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.SystemMessages;
import com.illcode.meterman2.model.TopicMap.Topic;

import java.util.*;

import static com.illcode.meterman2.Meterman2.gm;
import static com.illcode.meterman2.Meterman2.bundles;
import static com.illcode.meterman2.Meterman2.ui;

public class TalkSupport
{
    private Talker talker;

    private TopicMap topicMap;
    private Collection<Topic> currentTopics;
    private Topic otherTopic;
    private boolean checkOtherTopic;

    private List<Topic> getTopicsList; // for use with getTopics() to avoid allocation

    /**
     * Create a talk-support instance for the given talker.
     * @param talker the talker who we're supporting
     */
    public TalkSupport(Talker talker) {
        this.talker = talker;
        currentTopics = new LinkedHashSet<>();
        checkOtherTopic = true;
        getTopicsList = new ArrayList<>();
    }

    public TopicMap getTopicMap() {
        return topicMap;
    }

    public void setTopicMap(TopicMap topicMap) {
        this.topicMap = topicMap;
    }

    public void talk() {
        final List<Topic> topics = getTopics();
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

    public void addTopic(String id) {
        final Topic t = topicMap.getTopic(id);
        if (t != null)
            currentTopics.add(t);
    }

    public void removeTopic(String id) {
        final Topic t = topicMap.getTopic(id);
        if (t != null)
            currentTopics.remove(t);
    }

    public void clearTopics() {
        currentTopics.clear();
    }

    private List<Topic> getTopics() {
        if (checkOtherTopic) {
            final String otherLabel = talker.getOtherTopicLabel();
            if (otherLabel != null)
                otherTopic = new Topic(TopicMap.OTHER_TOPIC_ID, otherLabel);
            checkOtherTopic = false;
        }
        getTopicsList.clear();
        getTopicsList.addAll(currentTopics);
        if (otherTopic != null)
            getTopicsList.add(otherTopic);
        return getTopicsList;
    }
}
