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
import io.openmessaging.message.Message;
import io.openmessaging.producer.Producer;

import java.util.ArrayList;
import java.util.List;

/**
 * BatchProducer
 *
 * author: gaohaoxiang
 * date: 2019/4/8
 */
public class BatchProducer {

    public static void main(String[] args) {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        Producer producer = messagingAccessPoint.createProducer();
        producer.start();

        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Message message = producer.createMessage("test_topic_0", "body".getBytes());
            messages.add(message);
        }

        producer.send(messages);
    }
}