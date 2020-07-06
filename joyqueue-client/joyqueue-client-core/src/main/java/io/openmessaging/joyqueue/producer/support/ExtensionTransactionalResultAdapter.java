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

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.TransactionMessageProducer;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import com.google.common.base.Preconditions;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import io.openmessaging.joyqueue.producer.ExtensionTransactionalResult;
import io.openmessaging.joyqueue.producer.message.MessageAdapter;
import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * ExtensionTransactionalResultAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/4
 */
public class ExtensionTransactionalResultAdapter implements ExtensionTransactionalResult {

    private String transactionId;
    private MessageProducer messageProducer;
    private TransactionMessageProducer transactionMessageProducer;

    public ExtensionTransactionalResultAdapter(MessageProducer messageProducer) {
        this(null, messageProducer);
    }

    public ExtensionTransactionalResultAdapter(String transactionId, MessageProducer messageProducer) {
        this.transactionId = transactionId;
        this.messageProducer = messageProducer;
        this.transactionMessageProducer = prepare(transactionId);
    }

    protected TransactionMessageProducer prepare(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            return messageProducer.beginTransaction();
        } else {
            return messageProducer.beginTransaction(transactionId);
        }
    }

    @Override
    public String transactionId() {
        return transactionId;
    }

    @Override
    public void commit() {
        try {
            transactionMessageProducer.commit();
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    @Override
    public void rollback() {
        try {
            transactionMessageProducer.rollback();
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    @Override
    public SendResult send(Message message) {
        try {
            Preconditions.checkArgument(message instanceof MessageAdapter, "message is not supported");

            MessageAdapter messageAdapter = (MessageAdapter) message;
            org.joyqueue.client.internal.producer.domain.SendResult sendResult = transactionMessageProducer.send(messageAdapter.getProduceMessage());
            return SendResultConverter.convert(sendResult);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    @Override
    public List<SendResult> send(List<Message> messages) {
        try {
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(messages), "messages can not be null");

            List<ProduceMessage> produceMessages = checkAndConvertMessage(messages);
            List<org.joyqueue.client.internal.producer.domain.SendResult> sendResults = transactionMessageProducer.batchSend(produceMessages);
            return SendResultConverter.convert(sendResults);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    protected List<ProduceMessage> checkAndConvertMessage(List<Message> messages) {
        List<ProduceMessage> result = Lists.newArrayListWithCapacity(messages.size());
        for (Message message : messages) {
            Preconditions.checkArgument(message instanceof MessageAdapter, "message is not supported");
            result.add(((MessageAdapter) message).getProduceMessage());
        }
        return result;
    }
}