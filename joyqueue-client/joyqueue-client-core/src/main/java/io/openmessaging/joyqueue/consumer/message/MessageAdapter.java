package io.openmessaging.joyqueue.consumer.message;

import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.openmessaging.KeyValue;
import io.openmessaging.consumer.MessageReceipt;
import io.openmessaging.extension.ExtensionHeader;
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

    private ConsumeMessage message;

    private Header header;
    private Optional<ExtensionHeader> extensionHeader;
    private KeyValue properties;
    private MessageReceipt receipt;

    public MessageAdapter(ConsumeMessage message) {
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

    }

    @Override
    public void setStringData(String data) {

    }

    @Override
    public String getStringData() {
        return message.getBody();
    }

    @Override
    public MessageReceipt getMessageReceipt() {
        if (receipt == null) {
            receipt = new MessageReceiptAdapter(message);
        }
        return receipt;
    }

    @Override
    public String toString() {
        return message.toString();
    }
}