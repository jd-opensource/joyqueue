package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * AddTopicEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class UpdateTopicEvent extends MetaEvent {

    private Topic oldTopic;
    private Topic newTopic;

    public UpdateTopicEvent() {

    }

    public UpdateTopicEvent(Topic oldTopic, Topic newTopic) {
        this.oldTopic = oldTopic;
        this.newTopic = newTopic;
    }

    public UpdateTopicEvent(EventType eventType, Topic oldTopic, Topic newTopic) {
        super(eventType);
        this.oldTopic = oldTopic;
        this.newTopic = newTopic;
    }

    public Topic getOldTopic() {
        return oldTopic;
    }

    public void setOldTopic(Topic oldTopic) {
        this.oldTopic = oldTopic;
    }

    public Topic getNewTopic() {
        return newTopic;
    }

    public void setNewTopic(Topic newTopic) {
        this.newTopic = newTopic;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_TOPIC.name();
    }
}