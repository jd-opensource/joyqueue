/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.broker.election;

import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.election.command.AppendEntriesRequest;
import org.joyqueue.broker.replication.ReplicaGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.store.replication.ReplicableStore;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/11
 */
public abstract class LeaderElection extends Service {
    private static Logger logger = LoggerFactory.getLogger(LeaderElection.class);

    protected ElectionConfig electionConfig;
    protected TopicPartitionGroup topicPartitionGroup;
    protected ElectionManager electionManager;
    protected ClusterManager clusterManager;

    protected int leaderId = ElectionNode.INVALID_NODE_ID;
    protected int localNodeId = ElectionNode.INVALID_NODE_ID;

    protected EventBus<ElectionEvent> electionEventManager;
    protected ElectionMetadataManager electionMetadataManager;

    protected ReplicaGroup replicaGroup;
    protected ReplicableStore replicableStore;

    /**
     * 获取参与选举的所有节点
     * @return 所有节点
     */
    public abstract Collection<DefaultElectionNode> getAllNodes();

    /**
     * 获取leader节点id
     */
    public int getLeaderId() {
        return leaderId;
    }

    public abstract void setLeaderId(int leaderId) throws Exception;

    /**
     * 选举集群增加节点
     * @param node 增加的节点
     */
    public void addNode(DefaultElectionNode node) throws ElectionException{
        replicaGroup.addNode(node);
    }

    /**
     * 删除集群增加节点
     * @param brokerId 待删除的broker id
     */
    public void removeNode(int brokerId) {
        replicaGroup.removeNode(brokerId);
    }

    /**
     * 当前节点是否是leader
     * @return 是否是leader
     */
    public boolean isLeader() {
        return leaderId == localNodeId;
    }

    /**
     * 获取复制组
     * @return 复制组
     */
    public ReplicaGroup getReplicaGroup() {
        return replicaGroup;
    }

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    /**
     * 更新元数据
     * @param leaderId leader id
     * @param term 任期
     */
    void updateMetadata(int leaderId, int term) {
        Set<Integer> isrId = new HashSet<>();

        long startTime = SystemClock.now();

        try {
            clusterManager.leaderReport(TopicName.parse(topicPartitionGroup.getTopic()),
                    topicPartitionGroup.getPartitionGroupId(), leaderId, isrId, term);
        } catch (Exception e) {
            logger.warn("Partition group {}/node {} report leader fail",
                    topicPartitionGroup, localNodeId, e);
        }

        logger.info("Leader report, topic is {}, group id is {}, leader is {}, term is {}, elapse {} ms",
                topicPartitionGroup.getTopic(), topicPartitionGroup.getPartitionGroupId(),
                leaderId, term, SystemClock.now() - startTime);
    }

    /**
     * 处理添加记录请求
     * @param request 添加记录请求
     * @return 返回命令
     */
    public abstract Command handleAppendEntriesRequest(AppendEntriesRequest request);

	public void stepDown(int term){}

    public int getLocalNodeId() {
        return localNodeId;
    }
}
