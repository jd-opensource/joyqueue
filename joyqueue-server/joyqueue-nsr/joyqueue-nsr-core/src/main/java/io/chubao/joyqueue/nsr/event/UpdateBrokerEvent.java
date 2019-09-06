package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * UpdateBrokerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class UpdateBrokerEvent extends MetaEvent {

    private Broker oldBroker;
    private Broker newBroker;

    public UpdateBrokerEvent(Broker oldBroker, Broker newBroker) {
        this.oldBroker = oldBroker;
        this.newBroker = newBroker;
    }

    public UpdateBrokerEvent(EventType eventType, Broker oldBroker, Broker newBroker) {
        super(eventType);
        this.oldBroker = oldBroker;
        this.newBroker = newBroker;
    }

    public Broker getOldBroker() {
        return oldBroker;
    }

    public void setOldBroker(Broker oldBroker) {
        this.oldBroker = oldBroker;
    }

    public Broker getNewBroker() {
        return newBroker;
    }

    public void setNewBroker(Broker newBroker) {
        this.newBroker = newBroker;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_BROKER.name();
    }
}