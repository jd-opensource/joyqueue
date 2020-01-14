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
package io.openmessaging.joyqueue.producer.message;

import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.openmessaging.message.Header;

/**
 * MessageHeaderAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class MessageHeaderAdapter implements Header {

    private ProduceMessage message;

    public MessageHeaderAdapter(ProduceMessage message) {
        this.message = message;
    }

    @Override
    public Header setDestination(String destination) {
        message.setTopic(destination);
        return this;
    }

    @Override
    public Header setMessageId(String messageId) {
        return this;
    }

    @Override
    public Header setBornTimestamp(long bornTimestamp) {
        return this;
    }

    @Override
    public Header setBornHost(String bornHost) {
        return this;
    }

    @Override
    public Header setPriority(short priority) {
        message.setPriority((byte) priority);
        return this;
    }

    @Override
    public Header setDurability(short durability) {
        return this;
    }

    @Override
    public Header setDeliveryCount(int deliveryCount) {
        return this;
    }

    @Override
    public Header setCompression(short compression) {
        return this;
    }

    @Override
    public String getDestination() {
        return message.getTopic();
    }

    @Override
    public String getMessageId() {
        return null;
    }

    @Override
    public long getBornTimestamp() {
        return 0;
    }

    @Override
    public String getBornHost() {
        return null;
    }

    @Override
    public short getPriority() {
        return message.getPriority();
    }

    @Override
    public short getDurability() {
        return 0;
    }

    @Override
    public int getDeliveryCount() {
        return 0;
    }

    @Override
    public short getCompression() {
        return 0;
    }
}