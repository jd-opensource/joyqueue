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
package io.openmessaging.joyqueue.consumer.message;

import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.openmessaging.message.Header;

/**
 * MessageHeaderAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class MessageHeaderAdapter implements Header {

    private ConsumeMessage message;

    public MessageHeaderAdapter(ConsumeMessage message) {
        this.message = message;
    }

    @Override
    public Header setDestination(String destination) {
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
        return message.getTopic().getFullName();
    }

    @Override
    public String getMessageId() {
        return String.valueOf(message.getIndex());
    }

    @Override
    public long getBornTimestamp() {
        return message.getStartTime();
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