package com.jd.journalq.client.samples.api.interceptor;

import com.jd.journalq.client.internal.common.ordered.Ordered;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendResult;
import com.jd.journalq.client.internal.producer.interceptor.ProduceContext;
import com.jd.journalq.client.internal.producer.interceptor.ProducerInterceptor;

import java.util.List;

/**
 * SimpleMessageFilterProducerInterceptor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/8
 */
// 简单消息过滤实现
public class SimpleMessageFilterProducerInterceptor implements ProducerInterceptor, Ordered {

    @Override
    public boolean preSend(ProduceContext context) {
        for (ProduceMessage message : context.getMessages()) {
            message.putAttribute(SimpleMessageFilterConsts.ENVIRONMENT_ATTR_KEY, SimpleMessageFilterConsts.CURRENT_ENVIRONMENT);
        }
        return true;
    }

    @Override
    public void postSend(ProduceContext context, List<SendResult> result) {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}