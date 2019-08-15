package io.chubao.joyqueue.client.internal.consumer.interceptor;

import io.chubao.joyqueue.client.internal.consumer.config.ConsumerConfig;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * ConsumerInvocation
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class ConsumerInvocation {

    private ConsumerConfig config;
    private String topic;
    private NameServerConfig nameServerConfig;
    private List<ConsumeMessage> messages;
    private ConsumerInterceptorManager consumerInterceptorManager;
    private ConsumerInvoker consumerInvoker;

    public ConsumerInvocation(ConsumerConfig config, String topic, NameServerConfig nameServerConfig, List<ConsumeMessage> messages,
                              ConsumerInterceptorManager consumerInterceptorManager, ConsumerInvoker consumerInvoker) {
        this.config = config;
        this.topic = topic;
        this.nameServerConfig = nameServerConfig;
        this.messages = messages;
        this.consumerInterceptorManager = consumerInterceptorManager;
        this.consumerInvoker = consumerInvoker;
    }

    public List<ConsumeReply> invoke() {
        ConsumeContext context = new ConsumeContext(topic, config.getApp(), nameServerConfig, Collections.unmodifiableList(messages));
        List<ConsumerInterceptor> interceptors = consumerInterceptorManager.getSortedInterceptors();

        if (CollectionUtils.isEmpty(interceptors)) {
            return consumerInvoker.invoke(context);
        }

        boolean isRejected = false;
        for (ConsumerInterceptor interceptor : interceptors) {
            if (!interceptor.preConsume(context)) {
                isRejected = true;
                continue;
            }
        }

        if (isRejected) {
            return consumerInvoker.reject(context);
        }

        List<ConsumeReply> result = consumerInvoker.invoke(context);
        for (ConsumerInterceptor interceptor : interceptors) {
            interceptor.postConsume(context, result);
        }

        return result;
    }
}