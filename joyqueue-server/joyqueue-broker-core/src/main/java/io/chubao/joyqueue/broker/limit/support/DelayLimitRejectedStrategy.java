package io.chubao.joyqueue.broker.limit.support;

import com.google.common.collect.Sets;
import io.chubao.joyqueue.broker.limit.LimitRejectedStrategy;
import io.chubao.joyqueue.broker.limit.domain.LimitContext;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.toolkit.delay.AbstractDelayedOperation;
import io.chubao.joyqueue.toolkit.delay.DelayedOperationKey;
import io.chubao.joyqueue.toolkit.delay.DelayedOperationManager;

/**
 * DelayLimitRejectedStrategy
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class DelayLimitRejectedStrategy implements LimitRejectedStrategy {

    private DelayedOperationManager delayPurgatory;

    public DelayLimitRejectedStrategy() {
        this.delayPurgatory = new DelayedOperationManager("joyqueue-limit-delayed");
        this.delayPurgatory.start();
    }

    @Override
    public Command execute(LimitContext context) {
        delayPurgatory.tryCompleteElseWatch(new AbstractDelayedOperation(context.getDelay()) {
            @Override
            protected void onComplete() {
                context.getTransport().acknowledge(context.getRequest(), context.getResponse());
            }
        }, Sets.newHashSet(new DelayedOperationKey()));
        return null;
    }

    @Override
    public String type() {
        return "delay";
    }
}