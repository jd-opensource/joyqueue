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

import io.openmessaging.ServiceLifeState;
import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.consumer.MessageReceipt;
import io.openmessaging.extension.Extension;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.joyqueue.consumer.ConsumerIndex;
import io.openmessaging.joyqueue.consumer.ExtensionConsumer;
import io.openmessaging.message.Message;

import java.util.List;
import java.util.Optional;

/**
 * ConsumerWrapper
 *
 * author: gaohaoxiang
 * date: 2019/5/14
 */
public class ConsumerWrapper implements ExtensionConsumer {

    private ExtensionConsumer delegate;
    private MessageAccessPointHolder messageAccessPointHolder;

    public ConsumerWrapper(ExtensionConsumer delegate, MessageAccessPointHolder messageAccessPointHolder) {
        this.delegate = delegate;
        this.messageAccessPointHolder = messageAccessPointHolder;
    }

    @Override
    public Message receive(short partition, long timeout) {
        return delegate.receive(partition, timeout);
    }

    @Override
    public List<Message> batchReceive(short partition, long timeout) {
        return delegate.batchReceive(partition, timeout);
    }

    @Override
    public Message receive(short partition, long index, long timeout) {
        return delegate.receive(partition, index, timeout);
    }

    @Override
    public List<Message> batchReceive(short partition, long index, long timeout) {
        return delegate.batchReceive(partition, index, timeout);
    }

    @Override
    public void resume() {
        delegate.resume();
    }

    @Override
    public void suspend() {
        delegate.suspend();
    }

    @Override
    public void suspend(long timeout) {
        delegate.suspend(timeout);
    }

    @Override
    public boolean isSuspended() {
        return delegate.isSuspended();
    }

    @Override
    public void bindQueue(String queueName) {
        delegate.bindQueue(queueName);
    }

    @Override
    public void bindQueue(String queueName, MessageListener listener) {
        delegate.bindQueue(queueName, listener);
    }

    @Override
    public void bindQueue(String queueName, BatchMessageListener listener) {
        delegate.bindQueue(queueName, listener);
    }

    @Override
    public void unbindQueue(String queueName) {
        delegate.unbindQueue(queueName);
    }

    @Override
    public boolean isBindQueue() {
        return delegate.isBindQueue();
    }

    @Override
    public String getBindQueue() {
        return delegate.getBindQueue();
    }

    @Override
    public void addInterceptor(ConsumerInterceptor interceptor) {
        delegate.addInterceptor(interceptor);
    }

    @Override
    public void removeInterceptor(ConsumerInterceptor interceptor) {
        delegate.removeInterceptor(interceptor);
    }

    @Override
    public Message receive(long timeout) {
        return delegate.receive(timeout);
    }

    @Override
    public List<Message> batchReceive(long timeout) {
        return delegate.batchReceive(timeout);
    }

    @Override
    public void ack(MessageReceipt receipt) {
        delegate.ack(receipt);
    }

    @Override
    public Optional<Extension> getExtension() {
        return delegate.getExtension();
    }

    @Override
    public void start() {
        delegate.start();
        messageAccessPointHolder.startConsumer();
    }

    @Override
    public void stop() {
        delegate.stop();
        messageAccessPointHolder.stopConsumer();
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
    public ConsumerIndex getIndex(short partition) {
        return delegate.getIndex(partition);
    }

    @Override
    public void batchAck(List<MessageReceipt> receiptList) {
        delegate.batchAck(receiptList);
    }

    @Override
    public void commitIndex(short partition, long index) {
        delegate.commitIndex(partition, index);
    }

    @Override
    public void commitMaxIndex(short partition) {
        delegate.commitMaxIndex(partition);
    }

    @Override
    public void commitMaxIndex() {
        delegate.commitMaxIndex();
    }

    @Override
    public void commitMinIndex(short partition) {
        delegate.commitMinIndex(partition);
    }

    @Override
    public void commitMinIndex() {
        delegate.commitMinIndex();
    }
}