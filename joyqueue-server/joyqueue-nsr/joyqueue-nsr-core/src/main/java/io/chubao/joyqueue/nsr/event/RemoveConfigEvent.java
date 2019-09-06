package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * AddDataCenterEvent
 * author: gaohaoxiang
 * date: 2019/9/3
 */
public class RemoveConfigEvent extends MetaEvent {

    private Config config;

    public RemoveConfigEvent() {

    }

    public RemoveConfigEvent(Config config) {
        this.config = config;
    }

    public RemoveConfigEvent(EventType eventType, Config config) {
        super(eventType);
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_CONFIG.name();
    }
}