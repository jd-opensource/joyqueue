package io.chubao.joyqueue.broker.limit.support;

import io.chubao.joyqueue.broker.limit.RateLimiter;

/**
 * RateLimiter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class DefaultRateLimiter implements RateLimiter {

    private com.google.common.util.concurrent.RateLimiter tpsRateLimiter;
    private com.google.common.util.concurrent.RateLimiter trafficRateLimiter;

    public DefaultRateLimiter(int tps, int traffic) {
        this.tpsRateLimiter = com.google.common.util.concurrent.RateLimiter.create(tps);
        this.trafficRateLimiter = com.google.common.util.concurrent.RateLimiter.create(traffic);
    }

    @Override
    public boolean tryAcquireTps() {
        return tryAcquireTps(1);
    }

    @Override
    public boolean tryAcquireTps(int tps) {
        return tpsRateLimiter.tryAcquire(tps);
    }

    @Override
    public boolean tryAcquireTraffic(int traffic) {
        if (traffic <= 0) {
            return true;
        }
        return trafficRateLimiter.tryAcquire(traffic);
    }
}