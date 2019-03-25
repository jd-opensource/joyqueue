package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetCommitResponse extends KafkaRequestOrResponse {

    private Table<String, Integer, OffsetMetadataAndError> commitStatus;

    public OffsetCommitResponse(Table<String, Integer, OffsetMetadataAndError> commitStatus) {
        this.commitStatus = commitStatus;
    }

    public Table<String, Integer, OffsetMetadataAndError> getCommitStatus() {
        return commitStatus;
    }

    public void setCommitStatus(Table<String, Integer, OffsetMetadataAndError> commitStatus) {
        this.commitStatus = commitStatus;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}
