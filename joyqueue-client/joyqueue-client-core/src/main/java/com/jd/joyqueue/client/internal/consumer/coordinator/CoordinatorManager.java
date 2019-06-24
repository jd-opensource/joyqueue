/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.joyqueue.client.internal.consumer.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.joyqueue.client.internal.cluster.ClusterClient;
import com.jd.joyqueue.client.internal.cluster.ClusterClientManager;
import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.network.command.FetchAssignedPartitionResponse;
import com.jd.joyqueue.network.command.FetchAssignedPartitionData;
import com.jd.joyqueue.network.command.FindCoordinatorResponse;
import com.jd.joyqueue.network.command.FindCoordinatorAckData;
import com.jd.joyqueue.network.domain.BrokerNode;
import com.jd.joyqueue.toolkit.service.Service;
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

    public FetchAssignedPartitionResponse fetchAssignedPartition(BrokerNode brokerNode, String topic, String app, boolean isNearBy, long sessionTimeout) {
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
        FindCoordinatorResponse findCoordinatorResponse = clusterClientManager.getOrCreateClient().findCoordinators(topics, app);
        Map<String, FindCoordinatorAckData> coordinators = findCoordinatorResponse.getCoordinators();
        Map<String, BrokerNode> result = Maps.newHashMap();

        for (Map.Entry<String, FindCoordinatorAckData> entry : coordinators.entrySet()) {
            String topic = entry.getKey();
            FindCoordinatorAckData findCoordinatorAckData = entry.getValue();
            if (!findCoordinatorAckData.getCode().equals(JoyQueueCode.SUCCESS)) {
                logger.error("find coordinator error, topic: {}, error: {}", topic, findCoordinatorAckData.getCode().getMessage());
            }

            result.put(topic, findCoordinatorAckData.getNode());
        }

        return result;
    }
}