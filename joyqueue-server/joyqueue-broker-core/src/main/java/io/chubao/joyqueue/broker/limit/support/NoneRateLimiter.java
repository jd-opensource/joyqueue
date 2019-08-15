package io.chubao.joyqueue.broker.limit.support;

import io.chubao.joyqueue.broker.limit.RateLimiter;

/**
 * NoneRateLimiter
 *
 * author: gaohaoxiang
 * date: 2019/5/17
 */
public class NoneRateLimiter implements RateLimiter {

    @Override
    public boolean tryAcquireTps() {
        return true;
    }

    @Override
    public boolean tryAcquireTps(int tps) {
        return true;
    }

    @Override
    public boolean tryAcquireTraffic(int traffic) {
        return true;
    }
}