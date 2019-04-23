package com.illcode.meterman2.model;

import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.loader.LoaderHelper;
import com.illcode.meterman2.text.TextSource;
import com.illcode.meterman2.util.Dialogs;
import org.jdom2.Element;
import org.mini2Dx.gdx.utils.ObjectMap;

import java.util.*;

import static com.illcode.meterman2.util.Dialogs.DialogPassage;
import static com.illcode.meterman2.util.Dialogs.DialogSequence;

/**
 * Used to store topics for interactive discussion.
 */
public final class TopicMap
{
    /** If a topic has an ID of "{@value}" and is the only topic in an interactor's current topics,
     * then it will be chosen without prompting the user to select a topic. */
    public static final String GREETING_TOPIC_ID = "GREETING";

    /** Special topic ID that indicates an "Other Topic" topic. */
    static final String OTHER_TOPIC_ID = "OTHER";

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
            if (id == null || label == null)
                continue;
            final Collection<String> addTopics = helper.getListValue("addTopics");
            final Collection<String> removeTopics = helper.getListValue("removeTopics");
            // Look for the topic's content--first one wins.
            for (Element e : topicEl.getChildren()) {
                switch (e.getName()) {
                case "text":
                    topics.put(id, new Topic(id, label, addTopics, removeTopics, b.elementTextSource(e)));
                    break;
                case "dialog":
                    topics.put(id, new Topic(id, label, addTopics, removeTopics, Dialogs.loadDialogPassage(b, e)));
                    break;
                case "sequence":
                    topics.put(id, new Topic(id, label, addTopics, removeTopics, Dialogs.loadDialogSequence(b, e)));
                    break;
                }
            }
        }
    }

    public static final class Topic
    {
        private final String id;
        private final String label;
        private final Collection<String> addTopics;
        private final Collection<String> removeTopics;

        // The three types of content a topic can have.
        private final TextSource text;
        private final DialogPassage dialog;
        private final DialogSequence sequence;

        public Topic(String id, String label, Collection<String> addTopics,
                     Collection<String> removeTopics, TextSource text) {
            this.id = id;
            this.label = label;
            this.addTopics = addTopics;
            this.removeTopics = removeTopics;
            this.text = text;
            dialog = null;
            sequence = null;
        }

        public Topic(String id, String label, Collection<String> addTopics,
                     Collection<String> removeTopics, DialogPassage dialog) {
            this.id = id;
            this.label = label;
            this.addTopics = addTopics;
            this.removeTopics = removeTopics;
            text = null;
            this.dialog = dialog;
            sequence = null;
        }

        public Topic(String id, String label, Collection<String> addTopics,
                     Collection<String> removeTopics, DialogSequence sequence) {
            this.id = id;
            this.label = label;
            this.addTopics = addTopics;
            this.removeTopics = removeTopics;
            text = null;
            dialog = null;
            this.sequence = sequence;
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

        public boolean isDialogTopic() {
            return dialog != null || sequence != null;
        }

        public void showDialog() {
            if (dialog != null)
                dialog.show();
            else if (sequence != null)
                Dialogs.showDialogSequence(sequence);
        }

        public TextSource getText() {
            return text != null ? text : XBundle.ERROR_TEXT_SOURCE;
        }

        public String toString() {
            return getLabel();
        }
    }
}
