package io.chubao.joyqueue.client.internal.consumer.support;

import io.chubao.joyqueue.client.internal.consumer.BatchMessageListener;
import io.chubao.joyqueue.client.internal.consumer.config.ConsumerConfig;
import io.chubao.joyqueue.client.internal.consumer.converter.ConsumeMessageConverter;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;
import io.chubao.joyqueue.client.internal.consumer.exception.IgnoreAckException;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumeContext;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumerInvoker;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.domain.ConsumerPolicy;
import io.chubao.joyqueue.network.command.RetryType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * BatchConsumerInvoker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class BatchConsumerInvoker implements ConsumerInvoker {

    protected static final Logger logger = LoggerFactory.getLogger(BatchConsumerInvoker.class);

    private ConsumerConfig config;
    private TopicMetadata topicMetadata;
    private ConsumerPolicy consumerPolicy;
    private List<ConsumeMessage> messages;
    private List<BatchMessageListener> listeners;
    private ExecutorService listenerExecutor;

    public BatchConsumerInvoker(ConsumerConfig config, TopicMetadata topicMetadata, ConsumerPolicy consumerPolicy,
                                List<ConsumeMessage> messages, List<BatchMessageListener> listeners, ExecutorService listenerExecutor) {
        this.config = config;
        this.topicMetadata = topicMetadata;
        this.consumerPolicy = consumerPolicy;
        this.messages = messages;
        this.listeners = listeners;
        this.listenerExecutor = listenerExecutor;
    }

    @Override
    public List<ConsumeReply> invoke(ConsumeContext context) {
        final List<ConsumeMessage> filteredMessage = context.getFilteredMessages();
        if (CollectionUtils.isEmpty(filteredMessage)) {
            return ConsumeMessageConverter.convertToReply(messages, RetryType.NONE);
        }

        Future<?> future = listenerExecutor.submit(new Runnable() {
            @Override
            public void run() {
                for (BatchMessageListener listener : listeners) {
                    listener.onMessage(filteredMessage);
                }
            }
        });

        RetryType retryType = RetryType.NONE;
        try {
            future.get(consumerPolicy.getAckTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.warn("execute batchMessageListener timeout, topic: {}, messages: {}, listeners: {}", topicMetadata.getTopic(), messages, listeners);
            retryType = RetryType.TIMEOUT;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IgnoreAckException) {
                logger.debug("execute batchMessageListener, ignore ack, topic: {}, messages: {}, listeners: {}", topicMetadata.getTopic(), messages, listeners);
                retryType = RetryType.TIMEOUT;
            } else {
                logger.error("execute batchMessageListener exception, topic: {}, messages: {}, listeners: {}", topicMetadata.getTopic(), messages, listeners, cause);
                retryType = RetryType.EXCEPTION;
            }
        } catch (Exception e) {
            logger.error("execute batchMessageListener exception, topic: {}, messages: {}, listeners: {}", topicMetadata.getTopic(), messages, listeners, e);
            retryType = RetryType.EXCEPTION;
        }

        return ConsumeMessageConverter.convertToReply(messages, retryType);
    }

    @Override
    public List<ConsumeReply> reject(ConsumeContext context) {
        logger.info("reject execute batchListener, topic: {}, messages: {}", topicMetadata.getTopic(), messages);
        return ConsumeMessageConverter.convertToReply(messages, RetryType.NONE);
    }
}