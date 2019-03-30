package com.illcode.meterman2.model;

public class TalkingEntity extends Entity implements Talker
{
    private TalkSupport talkSupport;

    protected TalkingEntity(String id, EntityImpl impl) {
        super(id, impl);
        talkSupport = new TalkSupport(this);
    }

    /** Create a talking entity with the given ID and a basic implemention. */
    public static TalkingEntity create(String id) {
        return create(id, new BaseEntityImpl());
    }

    /** Create a talking entity with the given ID and implemention. */
    public static TalkingEntity create(String id, EntityImpl impl) {
        return new TalkingEntity(id, impl);
    }

    @Override
    public Object getState() {
        final Object[] stateObjs = new Object[2];
        stateObjs[0] = talkSupport.getState();
        stateObjs[1] = super.getState();
        return stateObjs;
    }

    @Override
    public void restoreState(Object state) {
        final Object[] stateObjs = (Object[]) state;
        talkSupport.restoreState(stateObjs[0]);
        super.restoreState(stateObjs[1]);
    }

    //region -- Implement Talker --
    public Entity getTalkerEntity() {
        return this;
    }

    public TalkSupport getTalkSupport() {
        return talkSupport;
    }

    public boolean topicChosen(TopicMap.Topic t) {
        return false;
    }

    public String getOtherTopicLabel() {
        return null;
    }

    public void talkOther(String topic) {
        // empty
    }
    //endregion
}