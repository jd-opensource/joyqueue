package io.chubao.joyqueue.client.internal.producer.interceptor;

import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.producer.config.ProducerConfig;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * ProducerInvocation
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class ProducerInvocation {

    private ProducerConfig config;
    private NameServerConfig nameServerConfig;
    private TopicMetadata topicMetadata;
    private List<ProduceMessage> messages;
    private ProducerInterceptorManager producerInterceptorManager;
    private ProducerInvoker producerInvoker;

    public ProducerInvocation(ProducerConfig config, NameServerConfig nameServerConfig, TopicMetadata topicMetadata,
                              List<ProduceMessage> messages, ProducerInterceptorManager producerInterceptorManager, ProducerInvoker producerInvoker) {
        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.topicMetadata = topicMetadata;
        this.messages = messages;
        this.producerInterceptorManager = producerInterceptorManager;
        this.producerInvoker = producerInvoker;
    }

    public List<SendResult> invoke() {
        ProduceContext context = new ProduceContext(topicMetadata.getTopic(), config.getApp(), nameServerConfig, Collections.unmodifiableList(messages));
        List<ProducerInterceptor> interceptors = producerInterceptorManager.getSortedInterceptors();

        if (CollectionUtils.isEmpty(interceptors)) {
            return producerInvoker.invoke(context);
        }

        boolean isRejected = false;
        for (ProducerInterceptor interceptor : interceptors) {
            if (!interceptor.preSend(context)) {
                isRejected = true;
                continue;
            }
        }

        if (isRejected) {
            return producerInvoker.reject(context);
        }

        List<SendResult> result = producerInvoker.invoke(context);
        for (ProducerInterceptor interceptor : interceptors) {
            interceptor.postSend(context, result);
        }

        return result;
    }
}