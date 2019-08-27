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
package io.openmessaging.joyqueue.support;

import io.openmessaging.Future;
import io.openmessaging.ServiceLifeState;
import io.openmessaging.extension.Extension;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.joyqueue.producer.ExtensionProducer;
import io.openmessaging.joyqueue.producer.ExtensionTransactionalResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;
import io.openmessaging.producer.TransactionalResult;

import java.util.List;
import java.util.Optional;

/**
 * ProducerWrapper
 *
 * author: gaohaoxiang
 * date: 2019/5/14
 */
public class ProducerWrapper implements ExtensionProducer {

    private ExtensionProducer delegate;
    private MessageAccessPointHolder messageAccessPointHolder;

    public ProducerWrapper(ExtensionProducer delegate, MessageAccessPointHolder messageAccessPointHolder) {
        this.delegate = delegate;
        this.messageAccessPointHolder = messageAccessPointHolder;
    }

    @Override
    public ExtensionTransactionalResult prepare() {
        return delegate.prepare();
    }

    @Override
    public ExtensionTransactionalResult prepare(String transactionId) {
        return delegate.prepare(transactionId);
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
    public Optional<Extension> getExtension() {
        return delegate.getExtension();
    }

    @Override
    public void start() {
        delegate.start();
        messageAccessPointHolder.startProducer();
    }

    @Override
    public void stop() {
        delegate.stop();
        messageAccessPointHolder.stopProducer();
    }

    @Override
    public ServiceLifeState currentState() {
        return delegate.currentState();
    }

    @Override
    public QueueMetaData getQueueMetaData(String queueName) {
        return delegate.getQueueMetaData(queueName);
    }

    @Override
    public Message createMessage(String queueName, byte[] body) {
        return delegate.createMessage(queueName, body);
    }

    @Override
    public Message createMessage(String queueName, String body) {
        return delegate.createMessage(queueName, body);
    }
}