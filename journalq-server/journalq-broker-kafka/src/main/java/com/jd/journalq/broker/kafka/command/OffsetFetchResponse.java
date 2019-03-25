package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetFetchResponse extends KafkaRequestOrResponse {

    private Table<String, Integer, OffsetMetadataAndError> topicMetadataAndErrors;

    public OffsetFetchResponse() {

    }

    public OffsetFetchResponse(Table<String, Integer, OffsetMetadataAndError> topicMetadataAndErrors) {
        this.topicMetadataAndErrors = topicMetadataAndErrors;
    }

    public Table<String, Integer, OffsetMetadataAndError>  getTopicMetadataAndErrors() {
        return topicMetadataAndErrors;
    }

    public void setTopicMetadataAndErrors(Table<String, Integer, OffsetMetadataAndError> topicMetadataAndErrors) {
        this.topicMetadataAndErrors = topicMetadataAndErrors;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: " + this.getClass().getSimpleName());
        return builder.toString();
    }
}
