package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * UpdateConfigEvent
 * author: gaohaoxiang
 * date: 2019/9/3
 */
public class UpdateConfigEvent extends MetaEvent {

    private Config oldConfig;
    private Config newConfig;

    public UpdateConfigEvent() {

    }

    public UpdateConfigEvent(Config oldConfig, Config newConfig) {
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }

    public UpdateConfigEvent(EventType eventType, Config oldConfig, Config newConfig) {
        super(eventType);
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }

    public Config getOldConfig() {
        return oldConfig;
    }

    public void setOldConfig(Config oldConfig) {
        this.oldConfig = oldConfig;
    }

    public Config getNewConfig() {
        return newConfig;
    }

    public void setNewConfig(Config newConfig) {
        this.newConfig = newConfig;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_CONFIG.name();
    }
}