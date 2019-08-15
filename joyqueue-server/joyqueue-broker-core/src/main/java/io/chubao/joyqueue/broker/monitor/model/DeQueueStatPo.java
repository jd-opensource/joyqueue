package io.chubao.joyqueue.broker.monitor.model;

/**
 * 出队
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class DeQueueStatPo extends BasePo {

    private long total;
    private long totalSize;

    public DeQueueStatPo(long total, long totalSize) {
        this.total = total;
        this.totalSize = totalSize;
    }

    public DeQueueStatPo() {

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