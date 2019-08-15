package io.chubao.joyqueue.broker.index.command;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;
import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexQueryRequest extends JoyQueuePayload {
    private String app;
    private Map<String, List<Integer>> topicPartitions;

    public ConsumeIndexQueryRequest(String app, Map<String, List<Integer>> topicPartitions) {
        this.app = app;
        this.topicPartitions = topicPartitions;
    }

    public String getApp() {
        return app;
    }

    public Map<String, List<Integer>> getTopicPartitions() {
        return topicPartitions;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_REQUEST;
    }
}
