package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * AddDataCenterEvent
 * author: gaohaoxiang
 * date: 2019/9/3
 */
public class RemoveDataCenterEvent extends MetaEvent {

    private DataCenter dataCenter;

    public RemoveDataCenterEvent() {

    }

    public RemoveDataCenterEvent(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    public RemoveDataCenterEvent(EventType eventType, DataCenter dataCenter) {
        super(eventType);
        this.dataCenter = dataCenter;
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_DATACENTER.name();
    }
}