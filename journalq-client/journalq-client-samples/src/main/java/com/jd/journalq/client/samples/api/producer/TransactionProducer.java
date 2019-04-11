/**
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
package com.jd.journalq.client.samples.api.producer;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.journalq.JournalQBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQProducerBuiltinKeys;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.TransactionStateCheckListener;
import io.openmessaging.producer.TransactionalResult;

/**
 * TransactionProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class TransactionProducer {

    public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JournalQBuiltinKeys.ACCOUNT_KEY, "test_token");
        keyValue.put(JournalQProducerBuiltinKeys.TRANSACTION_TIMEOUT, 1000 * 10);

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:jmq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        // 事务补偿
        Producer producer = messagingAccessPoint.createProducer(new TransactionStateCheckListener() {
            @Override
            public void check(Message message, TransactionalContext context) {
                System.out.println(String.format("check, message: %s", message));
                context.commit();
            }
        });
        producer.start();

        Message message = producer.createMessage("test_topic_0", "body".getBytes());
        message.extensionHeader().get().setTransactionId("test_transactionId");
        TransactionalResult prepare = producer.prepare(message);
        prepare.commit();

        System.out.println(String.format("messageId: %s", message.header().getMessageId()));
    }
}