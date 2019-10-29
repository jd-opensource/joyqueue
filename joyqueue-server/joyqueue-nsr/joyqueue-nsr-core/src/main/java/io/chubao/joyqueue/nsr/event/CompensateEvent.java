package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.nsr.nameservice.NameServiceCache;

/**
 * CompensateEvent
 * author: gaohaoxiang
 * date: 2019/10/18
 */
public class CompensateEvent extends MetaEvent {

    private NameServiceCache oldCache;
    private NameServiceCache newCache;

    public CompensateEvent() {

    }

    public CompensateEvent(NameServiceCache oldCache, NameServiceCache newCache) {
        this.oldCache = oldCache;
        this.newCache = newCache;
    }

    public NameServiceCache getOldCache() {
        return oldCache;
    }

    public void setOldCache(NameServiceCache oldCache) {
        this.oldCache = oldCache;
    }

    public NameServiceCache getNewCache() {
        return newCache;
    }

    public void setNewCache(NameServiceCache newCache) {
        this.newCache = newCache;
    }

    @Override
    public String getTypeName() {
        return EventType.COMPENSATE.name();
    }
}