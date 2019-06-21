package com.jd.journalq.client.samples.api.producer;

import com.jd.journalq.client.internal.common.ordered.Ordered;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendResult;
import com.jd.journalq.client.internal.producer.interceptor.ProduceContext;
import com.jd.journalq.client.internal.producer.interceptor.ProducerInterceptor;

import java.util.List;

/**
 * JournalqSimpleProducerInterceptor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/8
 */
// Ordered接口提供getOrder方法，用于指定顺序，可以不实现
// context还有attributes等可使用，具体看com.jd.journalq.client.internal.producer.interceptor.ProduceContext
public class JournalqSimpleProducerInterceptor implements ProducerInterceptor, Ordered {

    @Override
    public boolean preSend(ProduceContext context) {
        System.out.println("preSend");

        // 循环一批消息，单条生产和批量生产都是按批拦截
        for (ProduceMessage message : context.getMessages()) {
        }

        // 返回true表示这批消息可以生产，返回false表示这批消息不可生产
        return true;
    }

    @Override
    public void postSend(ProduceContext context, List<SendResult> result) {
        System.out.println("postSend");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}