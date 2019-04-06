package com.illcode.meterman2.model;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.loader.LoaderHelper;
import com.illcode.meterman2.text.StringSource;
import com.illcode.meterman2.text.TextSource;
import org.jdom2.Element;
import org.mini2Dx.gdx.utils.ObjectMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to store topics for interactive discussion.
 */
public final class TopicMap
{
    /** If a topic has an ID of "{@value}" and is the only topic in an interactor's current topics,
     * then it will be chosen without prompting the user to select a topic. */
    public static final String GREETING_TOPIC_ID = "GREETING";

    /** Special topic ID that indicates an "Other Topic" topic. For game system use. */
    static final String OTHER_TOPIC_ID = "__OTHER_TOPIC__";

    private ObjectMap<String,Topic> topics;

    public TopicMap() {
        topics = new ObjectMap<>(16);
    }

    /** Return the topic with a given ID, or null if not found. */
    public Topic getTopic(String id) {
        return topics.get(id);
    }

    /** Put a topic with a given ID. */
    public void putTopic(String id, Topic t) {
        topics.put(id, t);
    }

    /** Clear all topics. */
    public void clearTopics() {
        topics.clear();
    }

    /**
     * Loads topics from a given XML element, replacing any topics currently in the map.
     * @param el XML element
     * @param b
     */
    public void loadFrom(Element el, XBundle b) {
        topics.clear();
        LoaderHelper helper = LoaderHelper.wrap(el);
        for (Element topicEl : el.getChildren("topic")) {
            helper.setWrappedElement(topicEl);
            final String id = topicEl.getAttributeValue("id");
            final String label = helper.getValue("label");
            final Element text = topicEl.getChild("text");
            if (id == null || label == null || text == null)
                continue;
            final Collection<String> addTopics = helper.getListValue("addTopics");
            final Collection<String> removeTopics = helper.getListValue("removeTopics");
            putTopic(id, new Topic(id, label, addTopics, removeTopics, b.elementTextSource(text)));
        }
    }

    public static final class Topic
    {
        private final String id;
        private final String label;
        private final Collection<String> addTopics;
        private final Collection<String> removeTopics;
        private final TextSource text;

        public Topic(String id, String label, Collection<String> addTopics,
                     Collection<String> removeTopics, TextSource text) {
            this.id = id;
            this.label = label;
            this.addTopics = addTopics;
            this.removeTopics = removeTopics;
            this.text = text;
        }

        // Constructor for internal processing.
        Topic(String id, String label) {
            this.id = id;
            this.label = label;
            this.addTopics = Collections.emptyList();
            this.removeTopics = Collections.emptyList();
            this.text = null;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public Collection<String> getAddTopics() {
            return addTopics;
        }

        public Collection<String> getRemoveTopics() {
            return removeTopics;
        }

        public TextSource getText() {
            return text != null ? text : XBundle.ERROR_TEXT_SOURCE;
        }

        public String toString() {
            return getLabel();
        }
    }
}
