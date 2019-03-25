package com.jd.journalq.client.internal.consumer.interceptor;

import com.jd.journalq.client.internal.common.interceptor.BaseInterceptor;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;

import java.util.List;

/**
 * ConsumerInterceptor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public interface ConsumerInterceptor extends BaseInterceptor {

    boolean preConsume(ConsumeContext context);

    void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies);
}