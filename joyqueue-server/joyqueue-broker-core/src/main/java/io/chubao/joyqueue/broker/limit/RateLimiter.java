package io.chubao.joyqueue.broker.limit;

/**
 * RateLimiter
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public interface RateLimiter {

    boolean tryAcquireTps();

    boolean tryAcquireTps(int tps);

    boolean tryAcquireTraffic(int traffic);
}