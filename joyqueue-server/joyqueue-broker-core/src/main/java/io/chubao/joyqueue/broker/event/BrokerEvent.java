package io.chubao.joyqueue.broker.event;

/**
 * BrokerEvent
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
public class BrokerEvent {

    private BrokerEventType type;

    public BrokerEvent(BrokerEventType type) {
        this.type = type;
    }

    public BrokerEventType getType() {
        return type;
    }

    public enum BrokerEventType {
        START,

        STOP,

        ;
    }
}