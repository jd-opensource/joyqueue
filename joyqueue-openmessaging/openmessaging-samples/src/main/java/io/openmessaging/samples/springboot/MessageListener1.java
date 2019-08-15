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
package io.openmessaging.samples.springboot;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;
import io.openmessaging.spring.boot.annotation.OMSMessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageListener1 {

    @OMSMessageListener(queueName = "test_topic_0")
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", message));
        context.ack();
    }

    @OMSMessageListener(queueName = "test_topic_1")
    public void onReceived(List<Message> messages, BatchMessageListener.Context context) {
        for (Message message : messages) {
            System.out.println(String.format("receive, message: %s", message));
        }
        context.ack();
    }
}