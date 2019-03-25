package com.jd.journalq.toolkit.delay;

/**
 * AbstractDelayedOperation
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/12
 */
public abstract class AbstractDelayedOperation extends DelayedOperation {

    public AbstractDelayedOperation(long delayMs) {
        super(delayMs);
    }

    @Override
    protected void onComplete() {

    }

    @Override
    protected void onExpiration() {

    }
}