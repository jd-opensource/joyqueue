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
import io.openmessaging.journalq.producer.ExtensionProducer;
import io.openmessaging.journalq.producer.ExtensionTransactionalResult;
import io.openmessaging.message.Message;
import io.openmessaging.producer.SendResult;

/**
 * FutureProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class ExtensionTransactionProducer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JournalQBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:jmq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        ExtensionProducer producer = (ExtensionProducer) messagingAccessPoint.createProducer();
        producer.start();

        ExtensionTransactionalResult transactionalResult = producer.prepare();

        // 可以发多条事务消息
        Message message = producer.createMessage("test_topic_0", "body".getBytes());

        SendResult sendResult = transactionalResult.send(message);
        System.out.println(sendResult.messageId());

        sendResult = transactionalResult.send(message);
        System.out.println(sendResult.messageId());

        transactionalResult.commit();

        System.in.read();
    }
}