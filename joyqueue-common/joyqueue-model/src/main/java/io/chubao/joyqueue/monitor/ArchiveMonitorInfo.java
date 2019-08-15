package io.chubao.joyqueue.monitor;

import java.io.Serializable;

public class ArchiveMonitorInfo implements Serializable {

    /**
     * consumer 消费待归档记录数
     **/
    private long consumeBacklog;
    /**
     * producer 生产待归档记录数
     *
     **/
    private long produceBacklog;

    public long getConsumeBacklog() {
        return consumeBacklog;
    }

    public void setConsumeBacklog(long consumeBacklog) {
        this.consumeBacklog = consumeBacklog;
    }

    public long getProduceBacklog() {
        return produceBacklog;
    }

    public void setProduceBacklog(long produceBacklog) {
        this.produceBacklog = produceBacklog;
    }
}
