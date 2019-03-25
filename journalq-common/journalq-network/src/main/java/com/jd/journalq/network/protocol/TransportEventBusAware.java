package com.jd.journalq.network.protocol;

import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.toolkit.concurrent.EventBus;

/**
 * 通信事件注入
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/15
 */
@Deprecated
public interface TransportEventBusAware {

    void setTransportEventBus(EventBus<TransportEvent> eventBus);
}