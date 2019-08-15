package io.chubao.joyqueue.client.samples.spring;

import io.openmessaging.interceptor.Context;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.message.Message;

/**
 * SimpleProducerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/3/8
 */
public class SimpleProducerInterceptor implements ProducerInterceptor {

    @Override
    public void preSend(Message message, Context attributes) {
        System.out.println(String.format("preSend, message: %s", message));
    }

    @Override
    public void postSend(Message message, Context attributes) {
        System.out.println(String.format("postSend, message: %s", message));
    }
}