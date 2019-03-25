package com.jd.journalq.client.internal.consumer.support;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.consumer.MessageListener;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.converter.ConsumeMessageConverter;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.exception.IgnoreAckException;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumeContext;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumerInvoker;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.network.command.RetryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * OnceConsumerInvoker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class OnceConsumerInvoker implements ConsumerInvoker {

    protected static final Logger logger = LoggerFactory.getLogger(OnceConsumerInvoker.class);

    private ConsumerConfig config;
    private TopicMetadata topicMetadata;
    private Consumer.ConsumerPolicy consumerPolicy;
    private List<ConsumeMessage> messages;
    private List<MessageListener> listeners;
    private ExecutorService listenerExecutor;

    public OnceConsumerInvoker(ConsumerConfig config, TopicMetadata topicMetadata, Consumer.ConsumerPolicy consumerPolicy, List<ConsumeMessage> messages, List<MessageListener> listeners, ExecutorService listenerExecutor) {
        this.config = config;
        this.topicMetadata = topicMetadata;
        this.consumerPolicy = consumerPolicy;
        this.messages = messages;
        this.listeners = listeners;
        this.listenerExecutor = listenerExecutor;
    }

    @Override
    public List<ConsumeReply> invoke(final ConsumeContext context) {
        List<ConsumeReply> result = Lists.newArrayListWithCapacity(messages.size());

        for (final ConsumeMessage message : messages) {
            Future<?> future = listenerExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (context.isFilteredMessage(message)) {
                        return;
                    }
                    for (MessageListener listener : listeners) {
                        listener.onMessage(message);
                    }
                }
            });

            RetryType retryType = RetryType.NONE;
            try {
                future.get(consumerPolicy.getAckTimeout(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                logger.warn("execute messageListener timeout, topic: {}, message: {}, listeners: {}", topicMetadata.getTopic(), message, listeners);
                retryType = RetryType.TIMEOUT;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IgnoreAckException) {
                    logger.debug("execute messageListener, ignore ack, topic: {}, message: {}, listeners: {}", topicMetadata.getTopic(), message, listeners);
                    retryType = RetryType.OTHER;
                } else {
                    logger.error("execute messageListener exception, topic: {}, message: {}, listeners: {}", topicMetadata.getTopic(), message, listeners, cause);
                    retryType = RetryType.EXCEPTION;
                }
            } catch (Exception e) {
                logger.error("execute messageListener exception, topic: {}, message: {}, listeners: {}", topicMetadata.getTopic(), message, listeners, e);
                retryType = RetryType.EXCEPTION;
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