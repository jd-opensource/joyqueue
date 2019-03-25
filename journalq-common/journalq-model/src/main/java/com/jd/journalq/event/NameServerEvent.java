package com.jd.journalq.event;

/**
 * 元数据变更通知事件
 * <p>
 * Created by chengzhiliang on 2018/8/31.
 */
public class NameServerEvent extends MetaEvent {

    protected Integer brokerId;
    protected MetaEvent metaEvent;

    public NameServerEvent() {
    }

    public NameServerEvent(MetaEvent event, Integer brokerId) {
        super(event.getEventType());
        this.brokerId = brokerId;
        this.metaEvent = event;
    }

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public void setMetaEvent(MetaEvent metaEvent) {
        this.metaEvent = metaEvent;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public MetaEvent getMetaEvent() {
        return metaEvent;
    }


    @Override
    public String toString() {
        return "NameServerEvent{" +
                "brokerId=" + brokerId +
                ", metaEvent=" + metaEvent +
                ", type=" + eventType +
                '}';
    }
}
