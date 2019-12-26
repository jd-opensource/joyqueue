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
package io.openmessaging.joyqueue.consumer.support;

import org.joyqueue.client.internal.consumer.MessageListener;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.exception.IgnoreAckException;
import io.openmessaging.joyqueue.consumer.message.MessageConverter;
import io.openmessaging.message.Message;

/**
 * MessageListenerAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class MessageListenerAdapter implements MessageListener {

    private io.openmessaging.consumer.MessageListener omsMessageListener;

    public MessageListenerAdapter(io.openmessaging.consumer.MessageListener omsMessageListener) {
        this.omsMessageListener = omsMessageListener;
    }

    @Override
    public void onMessage(ConsumeMessage message) {
        MessageListenerContextAdapter context = new MessageListenerContextAdapter();
        Message omsMessage = MessageConverter.convertMessage(message);
        omsMessageListener.onReceived(omsMessage, context);

        if (!context.isAck()) {
            throw new IgnoreAckException();
        }
    }

    @Override
    public int hashCode() {
        return omsMessageListener.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return omsMessageListener.equals(obj);
    }

    @Override
    public String toString() {
        return omsMessageListener.toString();
    }
}