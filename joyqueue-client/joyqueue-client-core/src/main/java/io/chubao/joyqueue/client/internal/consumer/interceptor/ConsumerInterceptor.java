package io.chubao.joyqueue.client.internal.consumer.interceptor;

import io.chubao.joyqueue.client.internal.common.interceptor.BaseInterceptor;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;

import java.util.List;

/**
 * ConsumerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public interface ConsumerInterceptor extends BaseInterceptor {

    boolean preConsume(ConsumeContext context);

    void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies);
}