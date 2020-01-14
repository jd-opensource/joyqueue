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
import io.openmessaging.KeyValue;
import io.openmessaging.consumer.MessageReceipt;
import io.openmessaging.extension.ExtensionHeader;
import io.openmessaging.joyqueue.message.EmptyMessageReceipt;
import io.openmessaging.joyqueue.message.ExtensionMessage;
import io.openmessaging.message.Header;

import java.util.Optional;

/**
 * MessageAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class MessageAdapter implements ExtensionMessage {

    private ProduceMessage message;

    private Header header;
    private Optional<ExtensionHeader> extensionHeader;
    private KeyValue properties;
    private MessageReceipt receipt;

    public MessageAdapter(ProduceMessage message) {
        this.message = message;
    }

    @Override
    public Header header() {
        if (header == null) {
            header = new MessageHeaderAdapter(message);
        }
        return header;
    }

    @Override
    public Optional<ExtensionHeader> extensionHeader() {
        if (extensionHeader == null) {
            extensionHeader = Optional.of(new MessageExtensionHeaderAdapter(message));
        }
        return extensionHeader;
    }

    @Override
    public KeyValue properties() {
        if (properties == null) {
            properties = new MessagePropertiesAdapter(message);
        }
        return properties;
    }

    @Override
    public byte[] getData() {
        return message.getBodyBytes();
    }

    @Override
    public void setData(byte[] data) {
        message.setBodyBytes(data);
    }

    @Override
    public void setStringData(String data) {
        message.setBodyBytes(null);
        message.setBody(data);
    }

    @Override
    public String getStringData() {
        return message.getBody();
    }

    @Override
    public MessageReceipt getMessageReceipt() {
        if (receipt == null) {
            receipt = new EmptyMessageReceipt();
        }
        return receipt;
    }

    @Override
    public String toString() {
        return message.toString();
    }

    public ProduceMessage getProduceMessage() {
        return message;
    }
}