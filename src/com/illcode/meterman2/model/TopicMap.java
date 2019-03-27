package com.illcode.meterman2.model;

import com.illcode.meterman2.text.StringSource;
import com.illcode.meterman2.text.TextSource;
import org.mini2Dx.gdx.utils.ObjectMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Used by {@code Talker}S to store topics for discussion.
 */
public final class TopicMap
{
    public static final String OTHER_TOPIC_ID = "__OTHER_TOPIC__";

    private ObjectMap<String,Topic> topics;

    public TopicMap() {
        topics = new ObjectMap<>(16);
    }

    public Topic getTopic(String id) {
        return topics.get(id);
    }

    public void putTopic(String id, Topic t) {
        topics.put(id, t);
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

        public String getText() {
            return text != null ? text.getText() : "[Topic ID " + id + "]";
        }

        public String toString() {
            return getLabel();
        }
    }
}
