package io.chubao.joyqueue.nsr.nameservice;

import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.nsr.message.MessageListener;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NameServiceEventAdapter
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class NameServiceEventListenerAdapter implements MessageListener<MetaEvent> {

    protected static final Logger logger = LoggerFactory.getLogger(NameServiceEventListenerAdapter.class);

    private EventBus<NameServerEvent> eventBus;

    public NameServiceEventListenerAdapter(EventBus<NameServerEvent> eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onEvent(MetaEvent event) {
        NameServerEvent nameServerEvent = new NameServerEvent();
        nameServerEvent.setMetaEvent(event);
        nameServerEvent.setEventType(event.getEventType());
        eventBus.inform(nameServerEvent);
    }
}