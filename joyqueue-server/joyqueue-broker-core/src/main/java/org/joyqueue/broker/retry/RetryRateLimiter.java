package org.joyqueue.broker.retry;

import org.joyqueue.broker.limit.RateLimiter;

/**
 * Retry rate limiter interface
 *
 **/
public interface RetryRateLimiter {

    /**
     * Get or create a rate limiter
     * @return null indicate no limit
     **/
    RateLimiter getOrCreate(String topic, String app);
}
