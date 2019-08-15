package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

/**
 * InitProducerIdRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class InitProducerIdRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private int transactionTimeout;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    @Override
    public int type() {
        return KafkaCommandType.INIT_PRODUCER_ID.getCode();
    }

    @Override
    public String toString() {
        return "InitProducerIdRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", transactionTimeout=" + transactionTimeout +
                '}';
    }
}