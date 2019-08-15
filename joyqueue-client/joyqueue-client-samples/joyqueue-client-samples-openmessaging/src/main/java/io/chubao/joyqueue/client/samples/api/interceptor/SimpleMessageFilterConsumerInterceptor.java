package io.chubao.joyqueue.client.samples.api.interceptor;

import io.chubao.joyqueue.client.internal.common.ordered.Ordered;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * SimpleMessageFilterConsumerInterceptor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/8
 */
// 简单消息过滤实现
public class SimpleMessageFilterConsumerInterceptor implements ConsumerInterceptor, Ordered {

    @Override
    public boolean preConsume(ConsumeContext context) {
        for (ConsumeMessage message : context.getMessages()) {
            String environment = message.getAttribute(SimpleMessageFilterConsts.ENVIRONMENT_ATTR_KEY);
            if (!StringUtils.equals(environment, SimpleMessageFilterConsts.CURRENT_ENVIRONMENT)) {
                context.filterMessage(message);
            }
        }
        return true;
    }

    @Override
    public void postConsume(ConsumeContext context, List<ConsumeReply> consumeReplies) {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}