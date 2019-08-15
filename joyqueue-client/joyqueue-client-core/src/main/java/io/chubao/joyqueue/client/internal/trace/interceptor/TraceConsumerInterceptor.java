package io.chubao.joyqueue.client.internal.trace.interceptor;

import io.chubao.joyqueue.client.internal.common.ordered.Ordered;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import io.chubao.joyqueue.client.internal.trace.TraceBuilder;
import io.chubao.joyqueue.client.internal.trace.TraceCaller;
import io.chubao.joyqueue.client.internal.trace.TraceType;
import io.chubao.joyqueue.network.command.RetryType;

import java.util.List;

/**
 * TraceConsumerInterceptor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class TraceConsumerInterceptor implements ConsumerInterceptor, Ordered {

    private static final String CALLER_KEY = "_TRACE_CALLER_";

    @Override
    public boolean preConsume(ConsumeContext context) {
        TraceCaller caller = TraceBuilder.newInstance()
                .topic(context.getTopic())
                .app(context.getApp())
                .namespace(context.getNameserver().getNamespace())
                .type(TraceType.CONSUMER_CONSUME)
                .begin();
        context.putAttribute(CALLER_KEY, caller);
        return true;
    }

    @Override
    public void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies) {
        TraceCaller caller = context.getAttribute(CALLER_KEY);
        if (caller == null) {
            return;
        }

        for (ConsumeReply consumeReply : consumeReplies) {
            if (consumeReply.getRetryType().equals(RetryType.NONE)) {
                caller.end();
            } else {
                caller.error();
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}