package io.chubao.joyqueue.broker.monitor.model;

/**
 * 入队
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class EnQueueStatPo extends BasePo {

    private long total;
    private long totalSize;

    public EnQueueStatPo(long total, long totalSize) {
        this.total = total;
        this.totalSize = totalSize;
    }

    public EnQueueStatPo() {

    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}