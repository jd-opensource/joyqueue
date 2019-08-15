package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;
import java.util.Map;

/**
 * AddPartitionsToTxnRequest
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class AddPartitionsToTxnRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private long producerId;
    private short producerEpoch;
    private Map<String, List<Integer>> partitions;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public long getProducerId() {
        return producerId;
    }

    public void setProducerId(long producerId) {
        this.producerId = producerId;
    }

    public short getProducerEpoch() {
        return producerEpoch;
    }

    public void setProducerEpoch(short producerEpoch) {
        this.producerEpoch = producerEpoch;
    }

    public Map<String, List<Integer>> getPartitions() {
        return partitions;
    }

    public void setPartitions(Map<String, List<Integer>> partitions) {
        this.partitions = partitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_PARTITIONS_TO_TXN.getCode();
    }

    @Override
    public String toString() {
        return "AddPartitionsToTxnRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", partitions=" + partitions +
                '}';
    }
}