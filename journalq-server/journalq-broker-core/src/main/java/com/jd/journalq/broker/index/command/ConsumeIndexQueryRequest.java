package com.jd.journalq.broker.index.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;
import com.jd.journalq.common.network.command.CommandType;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexQueryRequest extends JMQPayload {
    private String app;
    private Map<String, Set<Integer>> topicPartitions;

    public ConsumeIndexQueryRequest(String app, Map<String, Set<Integer>> topicPartitions) {
        this.app = app;
        this.topicPartitions = topicPartitions;
    }

    public String getApp() {
        return app;
    }

    public Map<String, Set<Integer>> getTopicPartitions() {
        return topicPartitions;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_REQUEST;
    }
}
