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
package org.joyqueue.client.samples.springcloud.stream;

import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Test for Stream Listener
 */
@Service
public class StreamListenerService {

    @StreamListener(CustomProcessor.INPUT_ORDER)
    public void receiveMessage(Message<String> message) {
        System.out.println(String.format("接收到消息对象，headers=%s, payload=%s", message.getHeaders().toString(), message.getPayload()));
    }

    @StreamListener(CustomProcessor.INPUT_ORDER)
    public void receiveMessageBody(String receiveMsg) {
        System.out.println("接收到消息体: " + receiveMsg);
    }

}
