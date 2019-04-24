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
package com.jd.journalq.client.samples.api.consumer;

import com.jd.journalq.toolkit.network.IpUtil;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.journalq.JournalQBuiltinKeys;
import io.openmessaging.message.Message;

/**
 * ReceiveConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/20
 */
public class ReceiveConsumer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(JournalQBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(String.format("oms:journalq://test_app@%s:50088/UNKNOWN", IpUtil.getLocalIp()), keyValue);

        Consumer consumer = messagingAccessPoint.createConsumer();
        consumer.start();

        consumer.bindQueue("test_topic_0");

        while (true) {
            System.out.println("doReceive");
            Message message = consumer.receive(1000 * 10);

            if (message == null) {
                continue;
            }

            System.out.println(String.format("receive, message: %s", message));
            consumer.ack(message.getMessageReceipt());

            Thread.currentThread().sleep(1000 * 1);
        }
    }
}
