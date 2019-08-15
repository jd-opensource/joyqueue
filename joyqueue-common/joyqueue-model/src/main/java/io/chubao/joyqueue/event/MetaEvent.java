package io.chubao.joyqueue.event;

import java.io.Serializable;

/**
 * @author lixiaobin6
 */
public abstract class MetaEvent implements Serializable {
    protected EventType eventType;

    public MetaEvent() {
    }

    public MetaEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public abstract String getTypeName();
    @Override
    public String toString() {
        return "MetaEvent{" +
                "eventType=" + eventType +
                '}';
    }
}
