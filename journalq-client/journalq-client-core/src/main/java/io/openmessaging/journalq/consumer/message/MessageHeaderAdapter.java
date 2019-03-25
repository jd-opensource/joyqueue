package io.openmessaging.journalq.consumer.message;

import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import io.openmessaging.message.Header;

/**
 * MessageHeaderAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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