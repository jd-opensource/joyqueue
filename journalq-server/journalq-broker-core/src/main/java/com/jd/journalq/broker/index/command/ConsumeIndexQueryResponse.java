package com.jd.journalq.broker.index.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.broker.index.model.IndexMetadataAndError;


import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexQueryResponse extends JMQPayload {
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
