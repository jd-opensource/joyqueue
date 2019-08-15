package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.model.PartitionMetadataAndError;

import java.util.List;
import java.util.Map;

/**
 * TxnOffsetCommitRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class TxnOffsetCommitResponse extends KafkaRequestOrResponse {

    private Map<String, List<PartitionMetadataAndError>> partitions;

    public TxnOffsetCommitResponse() {

    }

    public TxnOffsetCommitResponse(Map<String, List<PartitionMetadataAndError>> partitions) {
        this.partitions = partitions;
    }

    public Map<String, List<PartitionMetadataAndError>> getPartitions() {
        return partitions;
    }

    public void setPartitions(Map<String, List<PartitionMetadataAndError>> partitions) {
        this.partitions = partitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.TXN_OFFSET_COMMIT.getCode();
    }
}
