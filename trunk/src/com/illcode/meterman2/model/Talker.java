package com.illcode.meterman2.model;

import com.illcode.meterman2.model.TopicMap.Topic;

/**
 * A game entity that can be talked to.
 */
public interface Talker
{
    /** Return the entity that is implementing, or bound to this, Talker. */
    Entity getTalkerEntity();

    /** Return the talk-support instance being used by this Talker. */
    TalkSupport getTalkSupport();

    /**
     * Called when the user has selected one of the active topics.
     * @param t topic chosen
     * @return false to show the topic text and process add- and remove-topics, as usual;
     * true if the Talker has handled this topic itself and wants to skip normal processing.
     */
    boolean topicChosen(Topic t);

    /**
     * Return non-null to display the "Other Topic" choice when we are talked to, with the
     * actual label being the return value of this method.
     */
    String getOtherTopicLabel();

    /**
     * If the user chooses to talk about "Other topic" he will be prompted to type something in,
     * and this method will be called with the typed text.
     * @param topic user-input topic
     */
    void talkOther(String topic);
}
