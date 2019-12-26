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
package io.openmessaging.joyqueue.producer.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.joyqueue.client.internal.MessageAccessPoint;
import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import io.openmessaging.Future;
import io.openmessaging.ServiceLifeState;
import io.openmessaging.extension.Extension;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import io.openmessaging.joyqueue.producer.ExtensionProducer;
import io.openmessaging.joyqueue.producer.ExtensionTransactionalResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;
import io.openmessaging.producer.TransactionStateCheckListener;
import io.openmessaging.producer.TransactionalResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * TransactionProducerImpl
 *
 * author: gaohaoxiang
 * date: 2019/2/22
 */
public class TransactionProducerImpl implements ExtensionProducer {

    private ExtensionProducer delegate;
    private TransactionStateCheckListener transactionStateCheckListener;
    private MessageProducer messageProducer;
    private MessageAccessPoint messageAccessPoint;
    private TxFeedbackConfig txFeedbackConfig;

    private Set<String> topics = Sets.newConcurrentHashSet();

    public TransactionProducerImpl(ExtensionProducer delegate, TransactionStateCheckListener transactionStateCheckListener, MessageProducer messageProducer,
                                             MessageAccessPoint messageAccessPoint, TxFeedbackConfig txFeedbackConfig) {
        this.delegate = delegate;
        this.transactionStateCheckListener = transactionStateCheckListener;
        this.messageProducer = messageProducer;
        this.messageAccessPoint = messageAccessPoint;
        this.txFeedbackConfig = txFeedbackConfig;
    }

    @Override
    public SendResult send(Message message) {
        return delegate.send(message);
    }

    @Override
    public Future<SendResult> sendAsync(Message message) {
        return delegate.sendAsync(message);
    }

    @Override
    public void sendOneway(Message message) {
        delegate.sendOneway(message);
    }

    @Override
    public void send(List<Message> messages) {
        delegate.send(messages);
    }

    @Override
    public Future<SendResult> sendAsync(List<Message> messages) {
        return delegate.sendAsync(messages);
    }

    @Override
    public void sendOneway(List<Message> messages) {
        delegate.sendOneway(messages);
    }

    @Override
    public void addInterceptor(ProducerInterceptor interceptor) {
        delegate.addInterceptor(interceptor);
    }

    @Override
    public void removeInterceptor(ProducerInterceptor interceptor) {
        delegate.removeInterceptor(interceptor);
    }

    @Override
    public TransactionalResult prepare(Message message) {
        return delegate.prepare(message);
    }

    @Override
    public ExtensionTransactionalResult prepare(String transactionId) {
        return delegate.prepare(transactionId);
    }

    @Override
    public ExtensionTransactionalResult prepare() {
        return delegate.prepare();
    }

    @Override
    public Message createMessage(String queueName, byte[] body) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");
            Preconditions.checkArgument(ArrayUtils.isNotEmpty(body), "body can not be null");

            doProcessCreateMessage(queueName);
            return delegate.createMessage(queueName, body);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    @Override
    public Message createMessage(String queueName, String body) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");
            Preconditions.checkArgument(StringUtils.isNotBlank(body), "body can not be null");

            doProcessCreateMessage(queueName);
            return delegate.createMessage(queueName, body);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    protected void doProcessCreateMessage(String queueName) {
        if (!topics.add(queueName)) {
            return;
        }
        messageAccessPoint.setTransactionCallback(queueName, txFeedbackConfig,
                new TransactionStateCheckListenerAdapter(transactionStateCheckListener));
    }

    @Override
    public Optional<Extension> getExtension() {
        return delegate.getExtension();
    }

    @Override
    public QueueMetaData getQueueMetaData(String queueName) {
        return delegate.getQueueMetaData(queueName);
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public ServiceLifeState currentState() {
        return delegate.currentState();
    }
}