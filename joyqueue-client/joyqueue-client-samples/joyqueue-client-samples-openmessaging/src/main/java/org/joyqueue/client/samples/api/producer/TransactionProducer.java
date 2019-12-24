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
package org.joyqueue.client.samples.api.producer;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.joyqueue.JoyQueueBuiltinKeys;
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.TransactionStateCheckListener;
import io.openmessaging.producer.TransactionalResult;

/**
 * TransactionProducer
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class TransactionProducer {

    public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");
        keyValue.put(JoyQueueBuiltinKeys.TRANSACTION_TIMEOUT, 1000 * 10);

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        // 事务补偿
        // 创建producer，如果是spring或springboot通过xml和注解方式添加
        // producer只会补偿发送过的主题的事务
        Producer producer = messagingAccessPoint.createProducer(new TransactionStateCheckListener() {
            @Override
            public void check(Message message, TransactionalContext context) {
                // 使用context的commit和rollback方法提交事务状态
                // 使用transactionId进行状态查询，这里只是简单例子，以具体业务为准
                String topic = message.header().getDestination();
                String transactionId = message.extensionHeader().get().getTransactionId();
                System.out.println(String.format("check, message: %s, transactionId: %s", message, transactionId));
                context.commit();
            }
        });
        producer.start();

        Message message = producer.createMessage("test_topic_0", "body".getBytes());

        // 添加事务id，设置过事务id的才会被补偿，补偿时会带上这个事务id，非必填
        // 建议根据业务使用有意义的事务id
        message.extensionHeader().get().setTransactionId("test_transactionId");

        // 事务prepare
        TransactionalResult transactionalResult = producer.prepare(message);

        // 提交事务
        transactionalResult.commit();

        // 回滚事务
//        transactionalResult.rollback();

        System.out.println(String.format("messageId: %s", transactionalResult.messageId()));
    }
}