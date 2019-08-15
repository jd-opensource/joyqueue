package io.chubao.joyqueue.network.protocol;

import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;

/**
 * TransportEventBusAware
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/15
 */
@Deprecated
public interface TransportEventBusAware {

    void setTransportEventBus(EventBus<TransportEvent> eventBus);
}