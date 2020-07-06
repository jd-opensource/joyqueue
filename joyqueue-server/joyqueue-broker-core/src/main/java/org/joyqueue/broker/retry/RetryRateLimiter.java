package org.joyqueue.broker.retry;

import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.toolkit.concurrent.EventListener;

/**
 * Retry rate limiter interface
 *
 **/
public interface RetryRateLimiter extends EventListener<MetaEvent> {

    /**
     * Get or create a rate limiter
     * @return null indicate no limit
     **/
    RateLimiter getOrCreate(String topic, String app);
}
