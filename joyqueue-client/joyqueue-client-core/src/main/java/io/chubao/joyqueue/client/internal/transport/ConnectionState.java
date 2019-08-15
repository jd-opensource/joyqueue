package io.chubao.joyqueue.client.internal.transport;

import com.google.common.collect.Sets;
import io.chubao.joyqueue.network.domain.BrokerNode;

import java.util.Collection;
import java.util.Set;

/**
 * ConnectionState
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/12
 */
public class ConnectionState {

    private Set<BrokerNode> brokerNodes = Sets.newConcurrentHashSet();
    private Set<String> topics = Sets.newConcurrentHashSet();
    private Set<String> apps = Sets.newConcurrentHashSet();

    public boolean addBrokerNode(BrokerNode brokerNode) {
        return brokerNodes.add(brokerNode);
    }

    public boolean addTopics(Collection<String> topics) {
        return this.topics.addAll(topics);
    }

    public boolean addApp(String app) {
        return apps.add(app);
    }

    public Set<BrokerNode> getBrokerNodes() {
        return brokerNodes;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public Set<String> getApps() {
        return apps;
    }
}