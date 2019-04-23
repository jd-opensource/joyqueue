/**
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
package com.jd.journalq.client.internal.consumer.support;

import com.jd.journalq.client.internal.consumer.BatchMessageListener;
import com.jd.journalq.client.internal.consumer.MessageListener;
import com.jd.journalq.client.internal.consumer.MessagePoller;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumerInterceptorManager;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumerInvocation;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.toolkit.concurrent.NamedThreadFactory;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TopicMessageConsumerDispatcher
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/25
 */
public class TopicMessageConsumerDispatcher extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TopicMessageConsumerDispatcher.class);

    private String topic;
    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private MessagePoller messagePoller;
    private MessageListenerManager messageListenerManager;
    private ConsumerInterceptorManager consumerInterceptorManager;
    private ExecutorService listenerExecutor;

    public TopicMessageConsumerDispatcher(String topic, ConsumerConfig config, NameServerConfig nameServerConfig,
                                          MessagePoller messagePoller, MessageListenerManager messageListenerManager, ConsumerInterceptorManager consumerInterceptorManager) {
        this.topic = topic;
        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.messagePoller = messagePoller;
        this.messageListenerManager = messageListenerManager;
        this.consumerInterceptorManager = consumerInterceptorManager;
    }

    @Override
    protected void doStart() throws Exception {
        listenerExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory(String.format("jmq-consumer-dispatch-%s", topic)));
    }

    @Override
    protected void doStop() {
        if (listenerExecutor != null) {
            listenerExecutor.shutdown();
        }
    }

    public boolean dispatch() {
        if (messageListenerManager.isEmpty()) {
            return false;
        }

        List<ConsumeMessage> messages = messagePoller.poll(topic);

        if (logger.isDebugEnabled()) {
            logger.debug("poll messages, topic: {}, app: {}, messages: {}", topic, nameServerConfig.getApp(), messages);
        }

        if (CollectionUtils.isEmpty(messages)) {
            return false;
        }

        TopicMetadata topicMetadata = messagePoller.getTopicMetadata(topic);
        Consumer.ConsumerPolicy consumerPolicy = topicMetadata.getConsumerPolicy();
        List<ConsumeReply> consumeReplies = doDispatch(topicMetadata, consumerPolicy, messages);

        if (logger.isDebugEnabled()) {
            logger.debug("reply messages, topic: {}, app: {}, replies: {}", topic, nameServerConfig.getApp(), consumeReplies);
        }

        messagePoller.reply(topic, consumeReplies);
        return true;
    }

    protected List<ConsumeReply> doDispatch(TopicMetadata topicMetadata, Consumer.ConsumerPolicy consumerPolicy, List<ConsumeMessage> messages) {
        List<MessageListener> listeners = messageListenerManager.getListeners();
        List<BatchMessageListener> batchListeners = messageListenerManager.getBatchListeners();

        if (CollectionUtils.isNotEmpty(batchListeners)) {
            return doBatchDispatch(topicMetadata, consumerPolicy, messages, batchListeners);
        } else {
            return doOnceDispatch(topicMetadata, consumerPolicy, messages, listeners);
        }
    }

    protected List<ConsumeReply> doBatchDispatch(TopicMetadata topicMetadata, final Consumer.ConsumerPolicy consumerPolicy,
                                                 final List<ConsumeMessage> messages, final List<BatchMessageListener> listeners) {
        return new ConsumerInvocation(config, topic, nameServerConfig, messages, consumerInterceptorManager,
                new BatchConsumerInvoker(config, topicMetadata, consumerPolicy, messages, listeners, listenerExecutor)).invoke();
    }

    protected List<ConsumeReply> doOnceDispatch(TopicMetadata topicMetadata, final Consumer.ConsumerPolicy consumerPolicy, final List<ConsumeMessage> messages, final List<MessageListener> listeners) {
        return new ConsumerInvocation(config, topic, nameServerConfig, messages, consumerInterceptorManager,
                new OnceConsumerInvoker(config, topicMetadata, consumerPolicy, messages, listeners, listenerExecutor)).invoke();
    }
}