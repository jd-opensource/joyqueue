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

import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.TransactionMessageProducer;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import io.openmessaging.joyqueue.producer.message.MessageAdapter;
import io.openmessaging.message.Message;
import io.openmessaging.producer.TransactionalResult;

/**
 * TransactionalResultAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class TransactionalResultAdapter implements TransactionalResult {

    private Message message;
    private String transactionId;
    private MessageProducer messageProducer;
    private TransactionMessageProducer transactionMessageProducer;

    public TransactionalResultAdapter(Message message, String transactionId, MessageProducer messageProducer) {
        this.message = message;
        this.transactionId = transactionId;
        this.messageProducer = messageProducer;
        this.transactionMessageProducer = prepare(message, transactionId, messageProducer);
    }

    protected TransactionMessageProducer prepare(Message message, String transactionId, MessageProducer messageProducer) {
        MessageAdapter messageAdapter = (MessageAdapter) message;
        ProduceMessage produceMessage = messageAdapter.getProduceMessage();
        TransactionMessageProducer transactionMessageProducer = messageProducer.beginTransaction(transactionId);
        transactionMessageProducer.send(produceMessage);
        return transactionMessageProducer;
    }

    @Override
    public String transactionId() {
        return transactionId;
    }

    @Override
    public void commit() {
        try {
            transactionMessageProducer.commit();
        } catch (Exception e) {
            throw ExceptionConverter.convertProduceException(e);
        }
    }

    @Override
    public void rollback() {
        try {
            transactionMessageProducer.rollback();
        } catch (Exception e) {
            throw ExceptionConverter.convertProduceException(e);
        }
    }

    @Override
    public String messageId() {
        return null;
    }
}