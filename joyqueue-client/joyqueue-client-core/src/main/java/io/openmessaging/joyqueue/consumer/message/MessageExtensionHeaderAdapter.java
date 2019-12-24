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
import io.openmessaging.extension.ExtensionHeader;

/**
 * MessageExtensionAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class MessageExtensionHeaderAdapter implements ExtensionHeader {

    private ConsumeMessage message;

    public MessageExtensionHeaderAdapter(ConsumeMessage message) {
        this.message = message;
    }

    @Override
    public ExtensionHeader setPartition(int partition) {
        return this;
    }

    @Override
    public ExtensionHeader setOffset(long offset) {
        return this;
    }

    @Override
    public ExtensionHeader setCorrelationId(String correlationId) {
        return this;
    }

    @Override
    public ExtensionHeader setTransactionId(String transactionId) {
        return this;
    }

    @Override
    public ExtensionHeader setStoreTimestamp(long storeTimestamp) {
        return this;
    }

    @Override
    public ExtensionHeader setStoreHost(String storeHost) {
        return this;
    }

    @Override
    public ExtensionHeader setMessageKey(String messageKey) {
        return this;
    }

    @Override
    public ExtensionHeader setTraceId(String traceId) {
        return this;
    }

    @Override
    public ExtensionHeader setDelayTime(long delayTime) {
        return this;
    }

    @Override
    public ExtensionHeader setExpireTime(long expireTime) {
        return this;
    }

    @Override
    public int getPartiton() {
        return message.getPartition();
    }

    @Override
    public long getOffset() {
        return message.getIndex();
    }

    @Override
    public String getCorrelationId() {
        return null;
    }

    @Override
    public String getTransactionId() {
        return null;
    }

    @Override
    public long getStoreTimestamp() {
        return 0;
    }

    @Override
    public String getStoreHost() {
        return null;
    }

    @Override
    public long getDelayTime() {
        return 0;
    }

    @Override
    public long getExpireTime() {
        return 0;
    }

    @Override
    public String getMessageKey() {
        return message.getBusinessId();
    }

    @Override
    public String getTraceId() {
        return null;
    }

    @Override
    public String toString() {
        return "MessageExtensionHeaderAdapter{" +
                "message=" + message +
                '}';
    }
}