package org.joyqueue.broker.event;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.service.Service;

/**
 * BrokerEventBus
 * author: gaohaoxiang
 * date: 2020/3/20
 */
public class BrokerEventBus extends Service {

    private EventBus eventBus = new EventBus("joyqueue-broker-eventBus");

    private BrokerContext brokerContext;

    public BrokerEventBus(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    @Override
    protected void validate() throws Exception {
        brokerContext.getStoreService().addListener((event) -> {
            publishEvent(event);
        });
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
        eventBus.add(event);
    }

    public void addListener(EventListener listener) {
        eventBus.addListener(listener);
    }

    public void removeListener(EventListener listener) {
        eventBus.removeListener(listener);
    }
}