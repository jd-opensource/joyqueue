package io.openmessaging.jmq.producer.message;

import com.jd.journalq.client.internal.producer.domain.ProduceMessage;

/**
 * MessageExtensionAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/1
 */
public class MessageExtensionHeaderAdapter implements ExtensionHeader {

    private ProduceMessage message;
    private String transactionId;

    public MessageExtensionHeaderAdapter(ProduceMessage message) {
        this.message = message;
    }

    @Override
    public ExtensionHeader setPartition(int partition) {
        message.setPartition((short) partition);
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
        this.transactionId = transactionId;
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
        message.setPartitionKey(messageKey);
        message.setBusinessId(messageKey);
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
    public ExtensionHeader setFlag(short flag) {
        message.setFlag(flag);
        return this;
    }

    @Override
    public int getPartiton() {
        return message.getPartition();
    }

    @Override
    public long getOffset() {
        return 0;
    }

    @Override
    public String getCorrelationId() {
        return null;
    }

    @Override
    public String getTransactionId() {
        return this.transactionId;
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
    public short getFlag() {
        return message.getFlag();
    }

    @Override
    public String toString() {
        return "MessageExtensionHeaderAdapter{" +
                "message=" + message +
                '}';
    }
}