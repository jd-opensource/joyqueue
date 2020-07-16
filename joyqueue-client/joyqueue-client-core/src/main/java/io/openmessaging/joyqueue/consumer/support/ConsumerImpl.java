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
package io.openmessaging.joyqueue.consumer.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.consumer.MessageReceipt;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.extension.Extension;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import io.openmessaging.joyqueue.consumer.ConsumerIndex;
import io.openmessaging.joyqueue.consumer.ExtensionConsumer;
import io.openmessaging.joyqueue.consumer.extension.ExtensionAdapter;
import io.openmessaging.joyqueue.consumer.message.MessageConverter;
import io.openmessaging.joyqueue.consumer.message.MessageReceiptAdapter;
import io.openmessaging.joyqueue.support.AbstractServiceLifecycle;
import io.openmessaging.message.Message;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.client.internal.consumer.MessageConsumer;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.domain.FetchIndexData;
import org.joyqueue.network.command.RetryType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ConsumerImpl
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class ConsumerImpl extends AbstractServiceLifecycle implements ExtensionConsumer {

    private MessageConsumer messageConsumer;
    private Optional<Extension> extension;

    public ConsumerImpl(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    protected void doStart() throws Exception {
        try {
            messageConsumer.start();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    protected void doStop() {
        try {
            messageConsumer.stop();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }

    }

    @Override
    public void resume() {
        try {
            messageConsumer.resumeListen();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void suspend() {
        try {
            messageConsumer.suspendListen();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void suspend(long timeout) {
        suspend();
    }

    @Override
    public boolean isSuspended() {
        try {
            return messageConsumer.isListenSuspended();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void bindQueue(String queueName) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");

            messageConsumer.subscribe(queueName);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void bindQueue(String queueName, MessageListener listener) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");
            Preconditions.checkArgument(listener != null, "listener can not be null");

            messageConsumer.subscribe(queueName, new MessageListenerAdapter(listener));
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void bindQueue(String queueName, BatchMessageListener listener) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");
            Preconditions.checkArgument(listener != null, "listener can not be null");

            messageConsumer.subscribeBatch(queueName, new BatchMessageListenerAdapter(listener));
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void unbindQueue(String queueName) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");

            if (!queueName.equals(messageConsumer.subscription())) {
                return;
            }
            messageConsumer.unsubscribe();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public boolean isBindQueue() {
        try {
            return messageConsumer.isSubscribed();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public String getBindQueue() {
        try {
            return messageConsumer.subscription();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void addInterceptor(ConsumerInterceptor interceptor) {
        try {
            Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

            messageConsumer.addInterceptor(new ConsumerInterceptorAdapter(interceptor));
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void removeInterceptor(ConsumerInterceptor interceptor) {
        try {
            Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

            messageConsumer.removeInterceptor(new ConsumerInterceptorAdapter(interceptor));
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }


    @Override
    public Message receive(long timeout) {
        try {
            ConsumeMessage consumeMessage = messageConsumer.pollOnce(timeout, TimeUnit.MILLISECONDS);
            if (consumeMessage == null) {
                return null;
            }
            return MessageConverter.convertMessage(consumeMessage);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public List<Message> batchReceive(long timeout) {
        try {
            List<ConsumeMessage> consumeMessages = messageConsumer.poll(timeout, TimeUnit.MILLISECONDS);
            if (CollectionUtils.isEmpty(consumeMessages)) {
                return Collections.emptyList();
            }
            return MessageConverter.convertMessages(consumeMessages);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public Message receive(short partition, long timeout) {
        try {
            ConsumeMessage consumeMessage = messageConsumer.pollPartitionOnce(partition, timeout, TimeUnit.MILLISECONDS);
            if (consumeMessage == null) {
                return null;
            }
            return MessageConverter.convertMessage(consumeMessage);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public List<Message> batchReceive(short partition, long timeout) {
        try {
            List<ConsumeMessage> consumeMessages = messageConsumer.pollPartition(partition, timeout, TimeUnit.MILLISECONDS);
            if (CollectionUtils.isEmpty(consumeMessages)) {
                return Collections.emptyList();
            }
            return MessageConverter.convertMessages(consumeMessages);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public Message receive(short partition, long index, long timeout) {
        try {
            ConsumeMessage consumeMessage = messageConsumer.pollPartitionOnce(partition, index, timeout, TimeUnit.MILLISECONDS);
            if (consumeMessage == null) {
                return null;
            }
            return MessageConverter.convertMessage(consumeMessage);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public List<Message> batchReceive(short partition, long index, long timeout) {
        try {
            List<ConsumeMessage> consumeMessages = messageConsumer.pollPartition(partition, index, timeout, TimeUnit.MILLISECONDS);
            if (CollectionUtils.isEmpty(consumeMessages)) {
                return Collections.emptyList();
            }
            return MessageConverter.convertMessages(consumeMessages);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void ack(MessageReceipt receipt) {
        try {
            Preconditions.checkArgument(receipt instanceof MessageReceiptAdapter, "receipt is not supported");

            MessageReceiptAdapter messageReceiptAdapter = (MessageReceiptAdapter) receipt;
            ConsumeMessage message = messageReceiptAdapter.getMessage();
            ConsumeReply consumeReply = new ConsumeReply(message.getPartition(), message.getIndex(), RetryType.NONE);
            messageConsumer.replyOnce(consumeReply);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public Optional<Extension> getExtension() {
        if (extension == null) {
            extension = Optional.of(new ExtensionAdapter(messageConsumer));
        }
        return extension;
    }

    @Override
    public QueueMetaData getQueueMetaData(String queueName) {
        return getExtension().get().getQueueMetaData(queueName);
    }

    @Override
    public ConsumerIndex getIndex(short partition) {
        FetchIndexData fetchIndexData = messageConsumer.fetchIndex(partition);
        return new ConsumerIndex(fetchIndexData.getIndex(), fetchIndexData.getLeftIndex(), fetchIndexData.getRightIndex());
    }

    @Override
    public void batchAck(List<MessageReceipt> receiptList) {
        try {
            List<ConsumeReply> replyList = Lists.newLinkedList();
            for (MessageReceipt receipt : receiptList) {
                Preconditions.checkArgument(receipt instanceof MessageReceiptAdapter, "receipt is not supported");

                MessageReceiptAdapter messageReceiptAdapter = (MessageReceiptAdapter) receipt;
                ConsumeMessage message = messageReceiptAdapter.getMessage();
                ConsumeReply consumeReply = new ConsumeReply(message.getPartition(), message.getIndex(), RetryType.NONE);
                replyList.add(consumeReply);
            }

            messageConsumer.reply(replyList);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void commitIndex(short partition, long index) {
        try {
            messageConsumer.commitIndex(partition, index);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void commitMaxIndex(short partition) {
        try {
            messageConsumer.commitMaxIndex(partition);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void commitMaxIndex() {
        try {
            messageConsumer.commitMaxIndex();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void commitMinIndex(short partition) {
        try {
            messageConsumer.commitMinIndex(partition);
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    @Override
    public void commitMinIndex() {
        try {
            messageConsumer.commitMinIndex();
        } catch (Throwable cause) {
            throw handleConsumeException(cause);
        }
    }

    protected OMSRuntimeException handleConsumeException(Throwable cause) {
        throw ExceptionConverter.convertConsumeException(cause);
    }
}