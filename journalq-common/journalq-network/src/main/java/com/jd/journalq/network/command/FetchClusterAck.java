package com.jd.journalq.network.command;

import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * GetClusterAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class FetchClusterAck extends JMQPayload {

    private Map<String, Topic> topics;
    private Map<Integer, BrokerNode> brokers;

    @Override
    public int type() {
        return JMQCommandType.FETCH_CLUSTER_ACK.getCode();
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
}