package com.jd.journalq.broker.limit.support;

import com.google.common.collect.Sets;
import com.jd.journalq.broker.limit.LimitRejectedStrategy;
import com.jd.journalq.broker.limit.domain.LimitContext;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.toolkit.delay.AbstractDelayedOperation;
import com.jd.journalq.toolkit.delay.DelayedOperationKey;
import com.jd.journalq.toolkit.delay.DelayedOperationManager;

/**
 * DelayLimitRejectedStrategy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class DelayLimitRejectedStrategy implements LimitRejectedStrategy {

    private DelayedOperationManager delayedOperationManager;

    public DelayLimitRejectedStrategy() {
        this.delayedOperationManager = new DelayedOperationManager("journalq-limit-delayed");
        this.delayedOperationManager.start();
    }

    @Override
    public Command execute(LimitContext context) {
        delayedOperationManager.tryCompleteElseWatch(new AbstractDelayedOperation(context.getDelay()) {
            @Override
            protected void onComplete() {
                context.getTransport().acknowledge(context.getRequest(), context.getResponse());
            }
        }, Sets.newHashSet(new DelayedOperationKey("limit_" + context.getTransport().toString())));
        return null;
    }

    @Override
    public String type() {
        return "delay";
    }
}