package com.jd.journalq.client.internal.consumer.trace;

import com.jd.journalq.client.internal.common.ordered.Ordered;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumeContext;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumerInterceptor;
import com.jd.journalq.client.internal.trace.TraceBuilder;
import com.jd.journalq.client.internal.trace.TraceCaller;
import com.jd.journalq.client.internal.trace.TraceType;
import com.jd.journalq.common.network.command.RetryType;

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