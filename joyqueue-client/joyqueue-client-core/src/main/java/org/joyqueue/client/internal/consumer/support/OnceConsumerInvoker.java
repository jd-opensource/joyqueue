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
package org.joyqueue.client.internal.consumer.support;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.consumer.MessageListener;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.converter.ConsumeMessageConverter;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.exception.IgnoreAckException;
import org.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInvoker;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.domain.ConsumerPolicy;
import org.joyqueue.network.command.RetryType;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OnceConsumerInvoker
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class OnceConsumerInvoker implements ConsumerInvoker {

    protected static final Logger logger = LoggerFactory.getLogger(OnceConsumerInvoker.class);

    private ConsumerConfig config;
    private TopicMetadata topicMetadata;
    private ConsumerPolicy consumerPolicy;
    private List<ConsumeMessage> messages;
    private List<MessageListener> listeners;

    public OnceConsumerInvoker(ConsumerConfig config, TopicMetadata topicMetadata, ConsumerPolicy consumerPolicy,
                               List<ConsumeMessage> messages, List<MessageListener> listeners) {
        this.config = config;
        this.topicMetadata = topicMetadata;
        this.consumerPolicy = consumerPolicy;
        this.messages = messages;
        this.listeners = listeners;
    }

    @Override
    public List<ConsumeReply> invoke(ConsumeContext context) {
        List<ConsumeReply> result = Lists.newArrayListWithCapacity(messages.size());

        for (ConsumeMessage message : messages) {
            RetryType retryType = RetryType.NONE;
            if (!context.isFilteredMessage(message)) {
                long ackTimeout = (config.getAckTimeout() != ConsumerConfig.NONE_ACK_TIMEOUT ? config.getAckTimeout() : consumerPolicy.getAckTimeout());
                try {
                    long startTime = SystemClock.now();
                    for (MessageListener listener : listeners) {
                        listener.onMessage(message);
                    }
                    long endTime = SystemClock.now();
                    if (endTime - startTime > ackTimeout) {
                        logger.warn("execute messageListener timeout, topic: {}, message: {}, listeners: {}", topicMetadata.getTopic(), message, listeners);
                        retryType = RetryType.NONE;
                    }
                } catch (Exception e) {
                    if (e instanceof IgnoreAckException) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("execute messageListener, ignore ack, topic: {}, message: {}, listeners: {}", topicMetadata.getTopic(), message, listeners);
                        }
                        if (config.isForceAck()) {
                            retryType = RetryType.OTHER;
                        } else {
                            retryType = RetryType.NONE;
                        }
                    } else {
                        logger.error("execute messageListener exception, topic: {}, message: {}, listeners: {}", topicMetadata.getTopic(), message, listeners, e);
                        retryType = RetryType.EXCEPTION;
                    }
                }
            }
            result.add(new ConsumeReply(message.getPartition(), message.getIndex(), retryType));
        }

        return result;
    }

    @Override
    public List<ConsumeReply> reject(ConsumeContext context) {
        logger.info("reject execute listener, topic: {}, messages: {}", topicMetadata.getTopic(), messages);
        return ConsumeMessageConverter.convertToReply(messages, RetryType.NONE);
    }
}