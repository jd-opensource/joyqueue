package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * UpdateDataCenterEvent
 * author: gaohaoxiang
 * date: 2019/9/3
 */
public class UpdateDataCenterEvent extends MetaEvent {

    private DataCenter oldDataCenter;
    private DataCenter newDataCenter;

    public UpdateDataCenterEvent() {

    }

    public UpdateDataCenterEvent(DataCenter oldDataCenter, DataCenter newDataCenter) {
        this.oldDataCenter = oldDataCenter;
        this.newDataCenter = newDataCenter;
    }

    public UpdateDataCenterEvent(EventType eventType, DataCenter oldDataCenter, DataCenter newDataCenter) {
        super(eventType);
        this.oldDataCenter = oldDataCenter;
        this.newDataCenter = newDataCenter;
    }

    public DataCenter getOldDataCenter() {
        return oldDataCenter;
    }

    public void setOldDataCenter(DataCenter oldDataCenter) {
        this.oldDataCenter = oldDataCenter;
    }

    public DataCenter getNewDataCenter() {
        return newDataCenter;
    }

    public void setNewDataCenter(DataCenter newDataCenter) {
        this.newDataCenter = newDataCenter;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_DATACENTER.name();
    }
}