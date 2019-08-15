package io.chubao.joyqueue.broker.monitor.stat;

import io.chubao.joyqueue.broker.monitor.metrics.Metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * DeQueueStat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class DeQueueStat {

    private LongAdder totalCount = new LongAdder();
    private LongAdder totalTraffic = new LongAdder();

    private Metrics count;
    private Metrics traffic;

    public DeQueueStat() {
        this.count = new Metrics();
        this.traffic = new Metrics();
    }

    public void slice() {
        this.count.slice();
        this.traffic.slice();
    }

    public void mark(double time, long size, long count) {
        this.count.mark(time, count);
        this.traffic.mark(size);
        this.totalCount.add(count);
        this.totalTraffic.add(size);
    }

    public void setTotal(long total) {
        this.totalCount.reset();
        this.totalCount.add(total);
    }

    public void setTotalSize(long totalSize) {
        this.totalTraffic.reset();
        this.totalTraffic.add(totalSize);
    }

    public long getTotal() {
        return this.totalCount.longValue();
    }

    @Deprecated
    public long getTotalSize() {
        return this.totalTraffic.longValue();
    }

    // 分钟
    @Deprecated
    public long getOneMinuteRate() {
        return this.count.getOneMinuteRate();
    }

    public double getTp99() {
        return this.count.getTp99();
    }

    public double getTp90() {
        return this.count.getTp90();
    }

    public double getMax() {
        return this.count.getMax();
    }

    public double getMin() {
        return this.count.getMin();
    }

    public double getAvg() {
        return this.count.getAvg();
    }

    // 分钟
    @Deprecated
    public long getSize() {
        return this.traffic.getOneMinuteRate();
    }

    public long getTps() {
        return this.count.getMeanRate();
    }

    public long getTraffic() {
        return this.traffic.getMeanRate();
    }

    public long getTotalTraffic() {
        return this.totalTraffic.longValue();
    }
}