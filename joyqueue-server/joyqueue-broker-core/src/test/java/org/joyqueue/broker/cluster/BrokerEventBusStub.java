package org.joyqueue.broker.cluster;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.event.BrokerEventBus;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.concurrent.EventListener;

/**
 * BrokerEventBusStub
 * author: gaohaoxiang
 * date: 2020/3/27
 */
public class BrokerEventBusStub extends BrokerEventBus {

    private EventBus eventBus = new EventBus("joyqueue-broker-eventBus");

    public BrokerEventBusStub(BrokerContext brokerContext) {
        super(brokerContext);
    }

    @Override
    protected void validate() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
        eventBus.start();
    }

    @Override
    protected void doStop() {
        eventBus.stop();
    }

    public void publishEvent(Object event) {
        eventBus.inform(event);
    }

    public void addListener(EventListener listener) {
        eventBus.addListener(listener);
    }

    public void removeListener(EventListener listener) {
        eventBus.removeListener(listener);
    }
}