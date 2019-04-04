package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;

import java.util.List;
import java.util.Map;

/**
 * AddPartitionsToTxnResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class AddPartitionsToTxnResponse extends KafkaRequestOrResponse {

    private Map<String, List<OffsetMetadataAndError>> errors;

    public void setErrors(Map<String, List<OffsetMetadataAndError>> errors) {
        this.errors = errors;
    }

    public Map<String, List<OffsetMetadataAndError>> getErrors() {
        return errors;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_PARTITIONS_TO_TXN.getCode();
    }
}