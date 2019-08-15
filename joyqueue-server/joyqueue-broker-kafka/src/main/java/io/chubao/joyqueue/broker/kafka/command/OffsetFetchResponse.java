package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.model.OffsetMetadataAndError;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetFetchResponse extends KafkaRequestOrResponse {

    private Map<String, List<OffsetMetadataAndError>> topicMetadataAndErrors;

    public OffsetFetchResponse() {

    }

    public OffsetFetchResponse(Map<String, List<OffsetMetadataAndError>> topicMetadataAndErrors) {
        this.topicMetadataAndErrors = topicMetadataAndErrors;
    }

    public void setTopicMetadataAndErrors(Map<String, List<OffsetMetadataAndError>> topicMetadataAndErrors) {
        this.topicMetadataAndErrors = topicMetadataAndErrors;
    }

    public Map<String, List<OffsetMetadataAndError>> getTopicMetadataAndErrors() {
        return topicMetadataAndErrors;
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
