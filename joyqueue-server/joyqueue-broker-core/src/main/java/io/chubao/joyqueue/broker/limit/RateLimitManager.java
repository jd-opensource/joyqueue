package io.chubao.joyqueue.broker.limit;

/**
 * RateLimitManager
 *
 * author: gaohaoxiang
 * date: 2019/5/17
 */
public interface RateLimitManager {

    RateLimiter getRateLimiter(String topic, String app, String type);
}