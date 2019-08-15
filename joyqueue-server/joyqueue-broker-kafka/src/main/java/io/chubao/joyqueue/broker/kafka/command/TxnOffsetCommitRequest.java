package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.model.OffsetAndMetadata;

import java.util.List;
import java.util.Map;

/**
 * TxnOffsetCommitRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class TxnOffsetCommitRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private String groupId;
    private long producerId;
    private short producerEpoch;
    private Map<String, List<OffsetAndMetadata>> partitions;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public void setPartitions(Map<String, List<OffsetAndMetadata>> partitions) {
        this.partitions = partitions;
    }

    public Map<String, List<OffsetAndMetadata>> getPartitions() {
        return partitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.TXN_OFFSET_COMMIT.getCode();
    }

    @Override
    public String toString() {
        return "TxnOffsetCommitRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", producerId=" + producerId +
                ", producerEpoch=" + producerEpoch +
                ", partitions=" + partitions +
                '}';
    }
}
