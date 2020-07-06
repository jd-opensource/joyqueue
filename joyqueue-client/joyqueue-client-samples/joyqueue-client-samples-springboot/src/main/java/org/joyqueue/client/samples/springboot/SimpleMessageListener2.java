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
package org.joyqueue.client.samples.springboot;

import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;
import io.openmessaging.spring.boot.annotation.OMSMessageListener;
import org.springframework.stereotype.Component;

/**
 * MessageListener2
 *
 * author: gaohaoxiang
 * date: 2019/3/6
 */
@Component
@OMSMessageListener(queueName = "test_topic_2")
public class SimpleMessageListener2 implements MessageListener {

    @Override
    public void onReceived(Message message, MessageListener.Context context) {
        System.out.println(String.format("receive, message: %s", message));
        context.ack();
    }
}