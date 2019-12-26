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
package org.joyqueue.client.samples.api.consumer;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;

/**
 * SimpleConsumer
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class SimpleConsumer {

    public static void main(String[] args) throws Exception {
        final String app = "test_app";
        final String token = "some token";
        final String dataCenter = "default";
        final String brokerAddr = "127.0.0.1:50088";
        final String topic = "test_topic_0";
        // oms:joyqueue://test_app@127.0.0.1:50088/default
        final String url = "oms:joyqueue://" + app +  "@" + brokerAddr + "/" + dataCenter;

        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, token);

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint(url, keyValue);

        // 创建consumer实例
        Consumer consumer = messagingAccessPoint.createConsumer();

        // 绑定需要消费的topic和对应的listener
        consumer.bindQueue(topic, new MessageListener() {
            @Override
            public void onReceived(Message message, Context context) {
                System.out.println(String.format("onReceived, message: %s", message));

                // 确认消息消费成功，如果没有确认或抛出异常会进入重试队列
                context.ack();
            }
        });

        consumer.start();
        System.in.read();
    }
}
