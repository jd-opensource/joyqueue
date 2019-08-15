package io.chubao.joyqueue.client.internal;

import io.chubao.joyqueue.client.internal.consumer.MessageConsumer;
import io.chubao.joyqueue.client.internal.consumer.MessagePoller;
import io.chubao.joyqueue.client.internal.consumer.config.ConsumerConfig;
import io.chubao.joyqueue.client.internal.producer.MessageProducer;
import io.chubao.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import io.chubao.joyqueue.client.internal.producer.config.ProducerConfig;
import io.chubao.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

/**
 * MessageAccessPoint
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public interface MessageAccessPoint extends LifeCycle {

    MessagePoller createPoller();

    MessagePoller createPoller(String group);

    MessagePoller createPoller(ConsumerConfig config);

    MessageConsumer createConsumer();

    MessageConsumer createConsumer(String group);

    MessageConsumer createConsumer(ConsumerConfig config);

    MessageProducer createProducer();

    MessageProducer createProducer(ProducerConfig config);

    void setTransactionCallback(String topic, TxFeedbackCallback callback);

    void setTransactionCallback(String topic, TxFeedbackConfig config, TxFeedbackCallback callback);

    void removeTransactionCallback(String topic);
}