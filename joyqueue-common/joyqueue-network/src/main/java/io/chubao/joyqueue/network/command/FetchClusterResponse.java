package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.domain.BrokerNode;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * GetClusterAck
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class FetchClusterResponse extends JoyQueuePayload {

    private Map<String, Topic> topics;
    private Map<Integer, BrokerNode> brokers;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_CLUSTER_RESPONSE.getCode();
    }

    public Map<String, Topic> getTopics() {
        return topics;
    }

    public void setTopics(Map<String, Topic> topics) {
        this.topics = topics;
    }

    public Map<Integer, BrokerNode> getBrokers() {
        return brokers;
    }

    public void setBrokers(Map<Integer, BrokerNode> brokers) {
        this.brokers = brokers;
    }

    @Override
    public String toString() {
        return "FetchClusterResponse{" +
                "topics=" + topics +
                ", brokers=" + brokers +
                '}';
    }
}