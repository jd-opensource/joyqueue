package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.model.PartitionMetadataAndError;

import java.util.List;
import java.util.Map;

/**
 * AddPartitionsToTxnResponse
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class AddPartitionsToTxnResponse extends KafkaRequestOrResponse {

    private Map<String, List<PartitionMetadataAndError>> errors;

    public AddPartitionsToTxnResponse() {

    }

    public AddPartitionsToTxnResponse(Map<String, List<PartitionMetadataAndError>> errors) {
        this.errors = errors;
    }

    public void setErrors(Map<String, List<PartitionMetadataAndError>> errors) {
        this.errors = errors;
    }

    public Map<String, List<PartitionMetadataAndError>> getErrors() {
        return errors;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_PARTITIONS_TO_TXN.getCode();
    }
}