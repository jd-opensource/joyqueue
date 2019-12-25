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

import org.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.TransactionStatus;
import org.joyqueue.domain.TopicName;
import io.openmessaging.joyqueue.producer.message.MessageAdapter;
import io.openmessaging.message.Message;
import io.openmessaging.producer.TransactionStateCheckListener;

/**
 * TransactionStateCheckListenerAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class TransactionStateCheckListenerAdapter implements TxFeedbackCallback {

    private TransactionStateCheckListener transactionStateCheckListener;

    public TransactionStateCheckListenerAdapter(TransactionStateCheckListener transactionStateCheckListener) {
        this.transactionStateCheckListener = transactionStateCheckListener;
    }

    @Override
    public TransactionStatus confirm(TopicName topic, String txId, String transactionId) {
        Message message = new MessageAdapter(new ProduceMessage());
        message.header()
                .setDestination(topic.getFullName());
        message.extensionHeader().get()
                .setTransactionId(transactionId);

        TransactionalContextAdapter transactionalContext = new TransactionalContextAdapter();
        transactionStateCheckListener.check(message, transactionalContext);

        if (transactionalContext.getStatus() == null) {
            return TransactionStatus.UNKNOWN;
        }
        return transactionalContext.getStatus();
    }
}