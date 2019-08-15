package io.openmessaging.joyqueue.producer.message;

import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
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