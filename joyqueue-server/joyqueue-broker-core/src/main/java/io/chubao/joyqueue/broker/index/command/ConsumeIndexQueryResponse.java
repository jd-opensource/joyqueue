package io.chubao.joyqueue.broker.index.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.broker.index.model.IndexMetadataAndError;


import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexQueryResponse extends JoyQueuePayload {
    Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndexs;

    public ConsumeIndexQueryResponse(Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndexs) {
        this.topicPartitionIndexs = topicPartitionIndexs;
    }

    public Map<String, Map<Integer, IndexMetadataAndError>> getTopicPartitionIndex() {
        return topicPartitionIndexs;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_RESPONSE;
    }
}
