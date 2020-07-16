/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.client.internal.consumer.interceptor;

import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * ConsumerInvocation
 *
 * author: gaohaoxiang
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
                break;
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