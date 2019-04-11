package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.PartitionMetadataAndError;

import java.util.List;
import java.util.Map;

/**
 * AddPartitionsToTxnResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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