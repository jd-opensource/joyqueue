package com.jd.journalq.common.event;

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

    @Override
    public String toString() {
        return "MetaEvent{" +
                "eventType=" + eventType +
                '}';
    }
}
