package com.jd.journalq.client.internal.consumer.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.cluster.ClusterClient;
import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAck;
import com.jd.journalq.common.network.command.FetchAssignedPartitionData;
import com.jd.journalq.common.network.command.FindCoordinatorAck;
import com.jd.journalq.common.network.command.FindCoordinatorAckData;
import com.jd.journalq.common.network.domain.BrokerNode;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * CoordinatorManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class CoordinatorManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorManager.class);

    private ClusterClientManager clusterClientManager;

    public CoordinatorManager(ClusterClientManager clusterClientManager) {
        this.clusterClientManager = clusterClientManager;
    }

    public FetchAssignedPartitionAck fetchAssignedPartition(BrokerNode brokerNode, String topic, String app, boolean isNearBy, long sessionTimeout) {
        ClusterClient client = null;
        try {
            client = clusterClientManager.createClient(brokerNode);
            return client.fetchAssignedPartition(Lists.newArrayList(new FetchAssignedPartitionData(topic, (int) sessionTimeout, isNearBy)), app);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public BrokerNode findCoordinator(String topic, String app) {
        Map<String, BrokerNode> coordinators = findCoordinators(Lists.newArrayList(topic), app);
        if (MapUtils.isEmpty(coordinators)) {
            return null;
        }
        return coordinators.get(topic);
    }

    public Map<String, BrokerNode> findCoordinators(List<String> topics, String app) {
        FindCoordinatorAck findCoordinatorAck = clusterClientManager.getOrCreateClient().findCoordinators(topics, app);
        Map<String, FindCoordinatorAckData> coordinators = findCoordinatorAck.getCoordinators();
        Map<String, BrokerNode> result = Maps.newHashMap();

        for (Map.Entry<String, FindCoordinatorAckData> entry : coordinators.entrySet()) {
            String topic = entry.getKey();
            FindCoordinatorAckData findCoordinatorAckData = entry.getValue();
            if (!findCoordinatorAckData.getCode().equals(JMQCode.SUCCESS)) {
                logger.error("find coordinator error, topic: {}, error: {}", topic, findCoordinatorAckData.getCode().getMessage());
            }

            result.put(topic, findCoordinatorAckData.getNode());
        }

        return result;
    }
}