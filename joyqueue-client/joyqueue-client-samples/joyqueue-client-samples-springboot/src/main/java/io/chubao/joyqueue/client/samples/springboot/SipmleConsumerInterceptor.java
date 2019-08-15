package io.chubao.joyqueue.client.samples.springboot;

import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.Context;
import io.openmessaging.message.Message;
import io.openmessaging.spring.boot.annotation.OMSInterceptor;

/**
 * SimpleConsumerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/3/8
 */
@OMSInterceptor
public class SipmleConsumerInterceptor implements ConsumerInterceptor {

    @Override
    public void preReceive(Message message, Context attributes) {
        System.out.println(String.format("preReceive, message: %s", message));
    }

    @Override
    public void postReceive(Message message, Context attributes) {
        System.out.println(String.format("postReceive, message: %s", message));
    }
}