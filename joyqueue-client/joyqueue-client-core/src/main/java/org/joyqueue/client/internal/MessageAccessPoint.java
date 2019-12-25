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
package org.joyqueue.client.internal;

import org.joyqueue.client.internal.consumer.MessageConsumer;
import org.joyqueue.client.internal.consumer.MessagePoller;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import org.joyqueue.toolkit.lang.LifeCycle;

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