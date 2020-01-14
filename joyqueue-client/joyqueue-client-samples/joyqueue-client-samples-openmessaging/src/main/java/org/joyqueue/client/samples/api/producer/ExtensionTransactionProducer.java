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
import io.openmessaging.joyqueue.producer.ExtensionProducer;
import io.openmessaging.joyqueue.producer.ExtensionTransactionalResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;

/**
 * FutureProducer
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class ExtensionTransactionProducer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        ExtensionProducer extensionProducer = (ExtensionProducer) messagingAccessPoint.createProducer();
        extensionProducer.start();

        ExtensionTransactionalResult transactionalResult = extensionProducer.prepare();

        for (int i = 0; i < 10; i++) {
            // 可以发多条事务消息
            Message message = extensionProducer.createMessage("test_topic_0", "body".getBytes());

            // 添加事务id，设置过事务id的才会被补偿，补偿时会带上这个事务id，非必填
            // 建议根据业务使用有意义的事务id
            message.extensionHeader().get().setTransactionId("test_transactionId");

            SendResult sendResult = transactionalResult.send(message);
            System.out.println(sendResult.messageId());
        }

        // 提交事务
        transactionalResult.commit();

        // 回滚事务
//        transactionalResult.rollback();
    }
}