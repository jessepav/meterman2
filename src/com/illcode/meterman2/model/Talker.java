package com.illcode.meterman2.model;

import com.illcode.meterman2.model.TopicMap.Topic;

import java.util.Collection;

/**
 * A game entity that can be talked to. Classes should delegate these methods
 * to an instance of {@link TalkSupport} in implementing this interface, for desired behavior:
 * <ul>
 *     <li>getTopicMap()</li>
 *     <li>setTopicMap()</li>
 *     <li>talk()</li>
 *     <li>addTopic()</li>
 *     <li>removeTopic()</li>
 *     <li>clearTopics()</li>
 * </ul>
 * and implement these themselves (or delegate them to scripts) to perform custom processing
 * <ul>
 *     <li>getTalkerEntity()</li>
 *     <li>topicChosen()</li>
 *     <li>getOtherTopicLabel()</li>
 *     <li>talkOther()</li>
 * </ul>
 */
public interface Talker
{
    /** Return the entity that is implementing Talker. */
    Entity getTalkerEntity();

    /**
     * Return the topic map being used by this Talker.
     */
    TopicMap getTopicMap();

    /**
     * Set the topic map being used by this Talker.
     */
    void setTopicMap(TopicMap topicMap);

    /**
     * Entry point to the conversation system, called by the game engine when the user
     * selects the Talk action on this entity.
     */
    void talk();

    /**
     * Hook method called when the user has selected one of the active topics.
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

    /**
     * Add a topic to our list of current topics.
     * @param id topic ID, as found in the topic map.
     */
    void addTopic(String id);

    /**
     * Remove a topic from our list of current topics.
     * @param id topic ID, as found in the topic map.
     */
    void removeTopic(String id);

    /**
     * Clear all current topics.
     */
    void clearTopics();
}
