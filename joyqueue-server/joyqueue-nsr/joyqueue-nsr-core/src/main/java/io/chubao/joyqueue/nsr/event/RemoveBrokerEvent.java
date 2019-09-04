package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * RemoveBrokerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class RemoveBrokerEvent extends MetaEvent {

    private Broker broker;

    public RemoveBrokerEvent() {

    }

    public RemoveBrokerEvent(Broker broker) {
        this.broker = broker;
    }

    public RemoveBrokerEvent(EventType eventType, Broker broker) {
        super(eventType);
        this.broker = broker;
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_BROKER.name();
    }
}