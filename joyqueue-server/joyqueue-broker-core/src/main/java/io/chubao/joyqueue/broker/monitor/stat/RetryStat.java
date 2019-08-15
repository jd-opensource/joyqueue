package io.chubao.joyqueue.broker.monitor.stat;

import io.chubao.joyqueue.broker.monitor.metrics.Metrics;

/**
 * RetryStat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class RetryStat {

    private Metrics success = new Metrics();
    private Metrics failure = new Metrics();

    public Metrics getSuccess() {
        return success;
    }

    public Metrics getFailure() {
        return failure;
    }
}